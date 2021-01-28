package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTextWebSocketClient extends TextWebSocketHandler {

    public final static ScheduledExecutorService CHECK_PONG_POOL=Executors.newScheduledThreadPool(1);

    protected StringBuilder cache=new StringBuilder();

    protected Logger logger= LoggerFactory.getLogger(this.getClass());
    protected String url;

    protected WebSocketSession session;

    protected WebSocketConnectionManager manager;

    //最后发送时间,当连接断开时候,如果最后发送时间距离当前时间超过1分钟,则不进行重连
    protected volatile long lastSendTimeInMillis;
    protected long maxReConnectTimeInMillis=60*1000;

    //是否停止连接
    //当断线后会检测是否应该重连,如果false,则设置此为true
    //当发送信息时候,如果检测到连接停止,则进行连接,并更新为false;
    protected AtomicBoolean isStop =new AtomicBoolean(false);

    //心跳检测线程池
    protected ScheduledExecutorService heartBeatPool;

    public WebSocketSession getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    public boolean isConnected(){
        return session!=null&&session.isOpen();
    }

    /**
     * 当连接建立之后
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Connect to [" + this.url + "] Succeed");
        initAfterConnectionEstablished(session);
    }

    /**
     * 在建立连接之后执行的初始化操作
     * @param session
     */
    protected synchronized void initAfterConnectionEstablished(WebSocketSession session){
        //1、赋值session
        this.session=session;
        //2、开启心跳检测
        startHeartBeatCheck();
        //3、激活所有正在等待的请求
        this.notifyAll();
    }

    /**
     * 开启心跳检测
     * 处理如下情况:
     * 1、由于长时间客户端和服务端不进行数据通信,此时由于防火墙等其他因素关闭tcp连接,导致不可用
     *      此时客户端需要发送ping,不关注结果,发送成功即代表session可用,否则会触发
     *      {@link java.net.SocketTimeoutException}即{@link IOException}
     *      如果发生异常、发送超时时候、session会自动关闭并触发close
     *      参考{@link org.apache.tomcat.websocket.WsRemoteEndpointImplBase#sendMessageBlock(byte, ByteBuffer, boolean, long)} }
     *
     *
     */
    protected void startHeartBeatCheck(){
        this.heartBeatPool= Executors.newSingleThreadScheduledExecutor();
        this.heartBeatPool.scheduleWithFixedDelay(()->{
            try {
                session.sendMessage(new PingMessage());
            } catch (IOException ex1) {
                logger.error("send ping failed", ex1);
            }
        },10,30, TimeUnit.SECONDS);
    }

    public BaseTextWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(throwable)->{
            onConnectFailed(throwable);
        });
        manager.start();
    }

    /**
     * 当连接失败之后
     * @param throwable
     */
    protected void onConnectFailed(Throwable throwable){
        manager.stop();
        boolean isReConnect= checkIsReConnectAfterDisConnect();
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
        return (System.currentTimeMillis()- lastSendTimeInMillis)<maxReConnectTimeInMillis;
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
        logger.error("WebSocket Connection Closed,Reason["+status+"]");
        destroyAfterConnectionClosed();
        this.manager.stop();
        boolean isReConnect=checkIsReConnectAfterDisConnect();
        if(isReConnect){
            logger.info("ReConnect AfterConnectionClosed");
            this.manager.start();
        }else{
            isStop.set(true);
        }
    }

    /**
     * 在断开连接之后执行的销毁操作
     */
    protected synchronized void destroyAfterConnectionClosed(){
        //1、置空session
        this.session=null;
        //2、清除发送的缓存
        cache.delete(0,cache.length());
        //3、关闭心跳检测线程池
        if(this.heartBeatPool!=null){
            this.heartBeatPool.shutdown();
            this.heartBeatPool=null;
        }
    }

    /**
     * 检测客户端是否已经停止,是则重启客户端并等待10s
     */
    protected void reConnectAndWaitOnSendMessage(){
        logger.info("Session Is DisConnect,Start It And Blocking SendMessage");
        synchronized (this){
            try {
                manager.start();
                this.wait(10*1000);
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 发送数据
     * @param data
     * @return
     */
    protected boolean sendMessage(String data){
        //1、更新最后发送时间
        lastSendTimeInMillis =System.currentTimeMillis();
        //2、检测客户端是否已经停止,是则重启客户端并等待10s
        if(isStop.compareAndSet(true,false)) {
            reConnectAndWaitOnSendMessage();
        }
        //3、发送数据
        if(isConnected()){
            TextMessage textMessage = new TextMessage(data);
            try {
                synchronized (this) {
                    if(isConnected()){
                        session.sendMessage(textMessage);
                        return true;
                    }else{
                        logger.error("Session Closed");
                        return false;
                    }
                }
            } catch (IOException e) {
                try {
                    //发送信息失败则关闭session
                    logger.error("send message error",e);
                    session.close(CloseStatus.NO_CLOSE_FRAME);
                } catch (IOException ex) {
                    logger.error("close session after send message error",e);
                }
                return false;
            }
        }else{
            logger.error("Session Closed");
            return false;
        }
    }

    /**
     * 当收到数据时候触发
     * @param data
     */
    public void onMessage(String data){

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
