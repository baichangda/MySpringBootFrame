package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.map.ExpireThreadSafeMap;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseJsonWebSocketClient<T> extends TextWebSocketHandler{

    public final ExpireThreadSafeMap<String,Consumer<String>> sn_to_callBack_map =new ExpireThreadSafeMap<>();

    protected Logger logger= LoggerFactory.getLogger(this.getClass());
    protected String url;

    protected WebSocketSession session;

    protected WebSocketConnectionManager manager;

    public WebSocketSession getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //1、赋值session
        this.session=session;
    }

    public BaseJsonWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(throwable)->{
            synchronized (this) {
                logger.error("Connect to [" + this.url + "] Failed,Will ReOpen After 10 Seconds", throwable);
                try {
                    Thread.sleep(10 * 1000L);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
                manager.stop();
                manager.start();
            }
        });
        manager.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            //1、转换结果集
            String jsonData=message.getPayload();
            JsonNode jsonNode= JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(jsonData);
            //2、触发onMessage方法
            onMessage(jsonNode.get("sn").asText(),jsonData);
        }catch (Exception ex){
            ExceptionUtil.printException(ex);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized (this) {
            logger.warn("WebSocket Connection Closed,Will Restart It");
            this.session = null;
            this.manager.stop();
            this.manager.start();
        }
    }


    /**
     *
     * @param param 参数
     * @param consumer webSocket回调(json字符串参数)
     * @param timeOutMills 超时时间(毫秒)
     * @param timeOutCallBack  webSocket超时回调
     * @param clazzs 结果泛型类型数组
     */
    public <R>boolean sendMessage(WebSocketData<T> param, Consumer<WebSocketData<R>> consumer, long timeOutMills, BiConsumer<String,Consumer<WebSocketData<R>>> timeOutCallBack, Class... clazzs){
        logger.info("Start WebSocket SN["+param.getSn()+"]");
        //1、绑定回调
        sn_to_callBack_map.put(param.getSn(),(v)->{
            try {
                JavaType tempType=null;
                for(int i=clazzs.length-2;i>=0;i--){
                    if(tempType==null){
                        tempType=TypeFactory.defaultInstance().constructParametricType(clazzs[i],clazzs[i+1]);
                    }else{
                        tempType=TypeFactory.defaultInstance().constructParametricType(clazzs[i],tempType);
                    }
                }
                JavaType resType= TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,tempType);
                WebSocketData<R> webSocketData= JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(v,resType);
                consumer.accept(webSocketData);
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        },timeOutMills,(k,v)->{
            logger.info("TimeOut WebSocket SN["+param.getSn()+"]");
            timeOutCallBack.accept(k,consumer);
        });
        //2、发送信息
        return sendMessage(param);
    }

    /**
     * 发送数据
     * @param param
     * @return
     */
    public boolean sendMessage(WebSocketData<T> param){
        if(session==null||!session.isOpen()){
            logger.error("Session Is Null Or Closed");
            return false;
        }else {
            String message = JsonUtil.toJson(param);
            TextMessage textMessage = new TextMessage(message);
            try {
                synchronized (this) {
                    if(session==null||!session.isOpen()){
                        logger.error("Session Is Null Or Closed");
                        return false;
                    }else{
                        logger.info("Send WebSocket SN[" + param.getSn() + "]");
                        session.sendMessage(textMessage);
                        return true;
                    }
                }
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 当收到数据时候触发
     * @param sn
     * @param data
     */
    public void onMessage(String sn,String data){
        logger.info("Receive WebSocket SN["+sn+"]");
        //1、取出流水号
        Consumer<String> consumer= sn_to_callBack_map.remove(sn);
        //2、触发回调
        if(consumer!=null) {
            consumer.accept(data);
        }
    }

    /**
     * 阻塞调用webSocket请求
     * @param paramData
     * @param timeOut
     * @param clazzs 返回参数泛型类型数组,只支持类型单泛型,例如:
     *               JsonMessage<<WebData<VehicleBean>>> 传参数 JsonMessage.class,WebData.class,VehicleBean.class
     * @return 返回null表示超时
     */
    public <R>WebSocketData<R> blockingRequest(T paramData, long timeOut, Class ... clazzs){
        WebSocketData<T> param = new WebSocketData<>(RandomStringUtils.randomAlphabetic(32), paramData);
        CountDownLatch countDownLatch=new CountDownLatch(1);
        WebSocketData<R>[] resData=new WebSocketData[1];
        sendMessage(param,(WebSocketData<R> res)->{
                resData[0]=res;
                countDownLatch.countDown();
        }, timeOut,(k, v)->{
            countDownLatch.countDown();
        },clazzs);
        try {
            countDownLatch.await();
            return resData[0];
        } catch (InterruptedException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
