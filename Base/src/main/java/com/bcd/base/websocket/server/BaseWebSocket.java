package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseWebSocket extends TextWebSocketHandler {

    protected String url;

    protected Logger logger= LoggerFactory.getLogger(getClass());

    protected WebSocketSession session;

    protected volatile long lastMessageTs=0L;

    protected Long pingIntervalMills;

    protected Long maxDisConnectMills;

    protected ScheduledExecutorService heartBeatWorker;

    protected void updateLastMessageTs(){
        if(maxDisConnectMills!=null){
            lastMessageTs=System.currentTimeMillis();
        }
    }

    public void startHeartBeat(long pingIntervalMills,long maxDisConnectMills){
        this.pingIntervalMills=pingIntervalMills;
        this.maxDisConnectMills=maxDisConnectMills;
    }

    public abstract CopyOnWriteArraySet<BaseWebSocket> getAll();


    public BaseWebSocket(String url) {
        this.url = url;
        startHeartBeat(10*1000,30*1000);
    }

    public String getUrl() {
        return url;
    }

    public WebSocketSession getSession() {
        return session;
    }



    /**
     * 连接打开时候触发
     * @param session
     */
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        this.session = session;
        getAll().add(this);
        updateLastMessageTs();
        //开始心跳定时任务
        if(this.maxDisConnectMills!=null) {
            this.heartBeatWorker =Executors.newSingleThreadScheduledExecutor();
            //开启定时发送ping
            this.heartBeatWorker.scheduleWithFixedDelay(()->{
                try {
                    session.sendMessage(new PingMessage());
                } catch (IOException e) {
                    ExceptionUtil.printException(e);
                }
            },1000L,pingIntervalMills,TimeUnit.MILLISECONDS);
            //开启定时检查pong
            this.heartBeatWorker.scheduleWithFixedDelay(() -> {
                if (System.currentTimeMillis() - lastMessageTs > maxDisConnectMills) {
                    try {
                        session.close();
                    } catch (IOException e) {
                        ExceptionUtil.printException(e);
                    }
                }
            }, 1000L, maxDisConnectMills / 2, TimeUnit.MILLISECONDS);
        }
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception{
        updateLastMessageTs();
        super.handleMessage(session,message);
    }


    /**
     * 连接关闭时候触发
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
        getAll().remove(this);
        //终止心跳定时任务
        if(this.maxDisConnectMills!=null) {
            if(this.heartBeatWorker !=null){
                this.heartBeatWorker.shutdown();
                this.heartBeatWorker =null;
            }
        }
    }

    /**
     * 发生错误时候触发
     * @param session
     * @param exception
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception{
        ExceptionUtil.printException(exception);
    }


    /**
     * 发送消息
     * @param message
     * @throws IOException
     */
    public boolean sendMessage(String message){
        try {
            if(session==null||!session.isOpen()){
                logger.error("Session Is Null Or Closed");
                return false;
            }else {
                synchronized (this) {
                    if(session==null||!session.isOpen()){
                        logger.error("Session Is Null Or Closed");
                        return false;
                    }else {
                        this.session.sendMessage(new TextMessage(message));
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
