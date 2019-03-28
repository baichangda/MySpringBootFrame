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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseJsonWebSocketClient<T> extends TextWebSocketHandler{

    public final ExpireThreadSafeMap<String,Consumer<String>> sn_to_callBack_map =new ExpireThreadSafeMap<>();

    /**
     * 0:session不可用
     * 1:正在初始化,session不可用
     * 2:初始化完成,session可用
     */
    AtomicInteger initStatus =new AtomicInteger(0);

    LinkedBlockingQueue<WebSocketData<T>> blockingMessageQueue=new LinkedBlockingQueue<>(10000);

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
        //1、更改状态为初始化中
        initStatus.set(1);
        //2、赋值session
        this.session=session;
        //3、更改状态为初始化完成
        initStatus.set(2);
        //4、进行阻塞数据发送
        WebSocketData<T> data;
        while ((data = blockingMessageQueue.poll()) != null) {
            if(sn_to_callBack_map.get(data.getSn())==null){
                return;
            }
            sendMessage(data);
        }

    }

    public BaseJsonWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(throwable)->{
            logger.error("Connect to ["+this.url+"] Failed,Will ReOpen After 10 Seconds",throwable);
            try {
                Thread.sleep(10*1000L);
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
            manager.stop();
            manager.start();
        });
        manager.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            //1、转换结果集
            String jsonData=message.getPayload();
            JsonNode jsonNode=JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(jsonData);
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
        initStatus.set(0);
        logger.warn("WebSocket Connection Closed,Will Restart It");
        this.session=null;
        this.manager.stop();
        this.manager.start();
    }


    /**
     *
     * @param param 参数
     * @param consumer webSocket回调(json字符串参数)
     * @param timeOutMills 超时时间(毫秒)
     * @param timeOutCallBack  webSocket超时回调
     * @param clazzs 结果泛型类型数组
     */
    public <R>void sendMessage(WebSocketData<T> param, Consumer<WebSocketData<R>> consumer, long timeOutMills, BiConsumer<String,Consumer<WebSocketData<R>>> timeOutCallBack, Class... clazzs){
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
                WebSocketData<R> webSocketData=JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(v,resType);
                consumer.accept(webSocketData);
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        },timeOutMills,(k,v)->{
            logger.info("TimeOut WebSocket SN["+param.getSn()+"]");
            blockingMessageQueue.remove(v);
            timeOutCallBack.accept(k,consumer);
        });
        //2、发送信息
        sendMessage(param);
    }

    public void sendMessage(WebSocketData<T> param){
        while(session==null||!session.isOpen()){
            int val= initStatus.get();
            switch (val){
                case 0:{
                    if(blockingMessageQueue.remainingCapacity()==0){
                        blockingMessageQueue.poll();
                    }
                    blockingMessageQueue.add(param);
                    logger.warn("RegisterWebSocketHandler Session Is Null Or Closed,Add Message To Queue");
                    break;
                }
                case 1:{
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                    break;
                }
                case 2:{
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                    break;
                }
                default:{
                    throw BaseRuntimeException.getException("Class["+this.getClass().getName()+"] initStatus["+val+"] Not Support");
                }
            }
        }
        String message=JsonUtil.toJson(param);
        TextMessage textMessage=new TextMessage(message);
        try {
            synchronized (session) {
                logger.info("Receive WebSocket SN["+param.getSn()+"]");
                session.sendMessage(textMessage);
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

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
    public <R>WebSocketData<R> blockingRequest(T paramData,long timeOut,Class ... clazzs){
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
