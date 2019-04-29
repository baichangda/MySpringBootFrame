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
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * webSocket客户端,存在如下机制
 * 1、断线重连机制(当连接断开以后会过10s后自动连接 (如果检测到发送时间距离当前时间1分钟之内) )
 * @param <T>
 */
public abstract class BaseJsonWebSocketClient<T> extends TextWebSocketHandler {

    public final StringBuilder cache=new StringBuilder();

    /**
     * 每3秒扫描一次过期的回调
     */
    public final ExpireThreadSafeMap<String,Consumer<String>> sn_to_callBack_map =new ExpireThreadSafeMap<>(3000L);

    protected Logger logger= LoggerFactory.getLogger(this.getClass());
    protected String url;

    protected WebSocketSession session;

    protected WebSocketConnectionManager manager;

    //最后发送时间,当连接断开时候,如果最后发送时间距离当前时间超过1分钟,则不进行重连
    protected volatile long lastSendTime;

    //是否停止连接
    //当断线后会检测是否应该重连,如果false,则设置此为true
    //当发送信息时候,如果检测到连接停止,则进行连接,并更新为false;
    protected AtomicBoolean isStop =new AtomicBoolean(false);

    public WebSocketSession getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 当连接建立之后
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        synchronized (this) {
            //1、赋值session
            this.session=session;
            //2、激活所有正在等待的请求
            this.notifyAll();
        }
    }

    public BaseJsonWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(s)->{
            onConnectSucceed(s);
        },(throwable)->{
            onConnectFailed(throwable);
        });
        manager.start();
    }

    /**
     * 当连接成功之后
     * @param session
     */
    protected void onConnectSucceed(WebSocketSession session){
        logger.info("Connect to [" + this.url + "] Succeed");
    }

    /**
     * 当连接失败之后
     * @param throwable
     */
    protected void onConnectFailed(Throwable throwable){
        manager.stop();
        boolean isReConnect= onConnectFailed();
        if(isReConnect){
            logger.error("Connect to [" + this.url + "] Failed,Will ReOpen After 10 Seconds", throwable);
            try {
                Thread.sleep(10 * 1000L);
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
            manager.start();
        }else{
            isStop.set(true);
            logger.error("Connect to [" + this.url + "] Failed,Don't ReConnect");
        }
    }

    /**
     * 在连接断开之后,是否重连
     * 返回true则进行重连,false则不进行
     * @return
     */
    protected boolean checkIsReConnectAfterDisConnect(){
        return (System.currentTimeMillis()-lastSendTime)<60*1000;
    }

    /**
     * 在连接失败之后执行
     * 返回true则进行重连,false则不进行
     * @return
     */
    protected boolean onConnectFailed(){
        return checkIsReConnectAfterDisConnect();
    }

    /**
     * 接收到pong message回调
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        logger.info("Receive PongMessage");
        super.handlePongMessage(session, message);
    }

    /**
     * 接收到文本信息回调
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if(supportsPartialMessages()){
            cache.append(message.getPayload());
            if(message.isLast()){
                String jsonData=cache.toString();
                cache.delete(0,cache.length());
                onMessage(jsonData);
            }
        }else{
            String jsonData=message.getPayload();
            onMessage(jsonData);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    /**
     * 当连接关闭之后
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized (this) {
            logger.error("WebSocket Connection Closed,Will Restart It");
            this.session = null;
        }
        this.manager.stop();
        boolean isReConnect=afterConnectionClosed();
        if(isReConnect){
            this.manager.start();
        }else{
            isStop.set(true);
        }
    }

    /**
     * 当连接关闭之后
     * 返回true则进行重连,false则不进行
     * @return
     */
    public boolean afterConnectionClosed(){
        cache.delete(0,cache.length());
        return checkIsReConnectAfterDisConnect();
    }

    /**
     * 检测客户端是否已经停止,是则重启客户端并等待10s
     */
    protected void reConnectOnSendMessage(){
        if(isStop.compareAndSet(true,false)) {
            logger.info("Session Is DisConnect,Start It And Blocking SendMessage");
            synchronized (this) {
                try {
                    manager.start();
                    this.wait(10 * 1000);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }
    }


    /**
     * @param param 参数
     * @param consumer webSocket回调(json字符串参数)
     * @param timeOutMills 超时时间(毫秒)
     * @param timeOutCallBack  webSocket超时回调
     * @param clazzs 结果泛型类型数组
     * @param <R>
     * @return 返回null表示发送失败;正常情况下返回发送过去的数据
     */
    public <R>WebSocketData<T> sendMessage(T param, Consumer<WebSocketData<R>> consumer, long timeOutMills, BiConsumer<String,Consumer<WebSocketData<R>>> timeOutCallBack, Class... clazzs){
        //1、开始
        WebSocketData<T> paramWebSocketData=new WebSocketData<>(RandomStringUtils.randomAlphabetic(32),param);
        logger.info("Start WebSocket SN["+paramWebSocketData.getSn()+"]");
        lastSendTime=System.currentTimeMillis();
        //1.1、检测客户端是否已经停止,是则重启客户端并等待10s
        reConnectOnSendMessage();
        //1.2、绑定回调
        sn_to_callBack_map.put(paramWebSocketData.getSn(), (v) -> {
            try {
                JavaType resType;
                if(clazzs.length==1){
                    resType=TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,clazzs);
                }else {
                    JavaType tempType=null;
                    for (int i = clazzs.length - 2; i >= 0; i--) {
                        if (tempType == null) {
                            tempType = TypeFactory.defaultInstance().constructParametricType(clazzs[i], clazzs[i + 1]);
                        } else {
                            tempType = TypeFactory.defaultInstance().constructParametricType(clazzs[i], tempType);
                        }
                    }
                    resType= TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,tempType);
                }
                WebSocketData<R> webSocketData = JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(v, resType);
                consumer.accept(webSocketData);
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        }, timeOutMills, (k, v) -> {
            logger.info("TimeOut WebSocket SN[" + paramWebSocketData.getSn() + "]");
            timeOutCallBack.accept(k, consumer);
        });
        //2、发送信息
        boolean sendRes= sendMessage(paramWebSocketData);
        //3、如果发送失败,则移除回调
        if(sendRes){
            return paramWebSocketData;
        }else{
            sn_to_callBack_map.remove(paramWebSocketData.getSn());
            return null;
        }
    }

    /**
     * 发送数据
     * @param param
     * @return
     */
    protected boolean sendMessage(WebSocketData<T> param){
        if(session==null||!session.isOpen()){
            logger.error("Session Closed");
            return false;
        }else {
            String message = JsonUtil.toJson(param);
            TextMessage textMessage = new TextMessage(message);
            try {
                synchronized (this) {
                    if(session==null||!session.isOpen()){
                        logger.error("Session Closed");
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
     * @param data
     */
    public void onMessage(String data){
        try {
            JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(data);
            String sn=jsonNode.get("sn").asText();
            logger.info("Receive WebSocket SN[" + sn + "]");
            //1、取出流水号
            Consumer<String> consumer = sn_to_callBack_map.remove(sn);
            //2、触发回调
            if (consumer == null) {
                logger.warn("Receive No Consumer Message SN[" + sn + "]");
            } else {
                consumer.accept(data);
            }
        }catch (Exception e){
            ExceptionUtil.printException(e);
        }
    }

    /**
     * 阻塞调用webSocket请求
     * 如果出现连接不可用,会抛出异常
     * @param paramData
     * @param timeOut
     * @param clazzs 返回参数泛型类型数组,只支持类型单泛型,例如:
     *               <WebData<VehicleBean>> 传参数 WebData.class,VehicleBean.class
     * @return 返回null表示发送超时
     */
    public <R>WebSocketData<R> blockingRequest(T paramData, long timeOut, Class ... clazzs){
        CountDownLatch countDownLatch=new CountDownLatch(1);
        WebSocketData<R>[] resData=new WebSocketData[1];
        WebSocketData<T> sendWebSocketData=sendMessage(paramData,(WebSocketData<R> res)->{
                resData[0]=res;
                countDownLatch.countDown();
        }, timeOut,(k, v)->{
            countDownLatch.countDown();
        },clazzs);
        if(sendWebSocketData==null){
            throw BaseRuntimeException.getException("Session Closed,WebSocket Send Failed");
        }else{
            try {
                countDownLatch.await();
                return resData[0];
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 是否支持分片信息
     * @return
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
