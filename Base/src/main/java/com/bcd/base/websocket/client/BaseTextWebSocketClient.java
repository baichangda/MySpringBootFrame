package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
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

public abstract class BaseTextWebSocketClient extends TextWebSocketHandler{

    public final StringBuilder cache=new StringBuilder();

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

    public BaseTextWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new MyWebSocketConnectionManager(client,this,url,(s)->{
            logger.info("Connect to [" + this.url + "] Succeed");
        },(throwable)->{
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
            cache.append(message.getPayload());
            if(message.isLast()){
                String data=cache.toString();
                cache.delete(0,cache.length());
                onMessage(session, data);
            }
        }catch (Exception ex){
            ExceptionUtil.printException(ex);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        logger.info("Pong Message: "+new String(message.getPayload().array()));
        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized (this) {
            logger.error("WebSocket Connection Closed,Will Restart It");
            this.session = null;
            this.manager.stop();
            cache.delete(0,cache.length());
            this.manager.start();
        }
    }

    /**
     * 发送文本
     * @param message
     * @return
     */
    public boolean sendMessage(String message){
        if(session==null||!session.isOpen()){
            logger.error("Session Is Null Or Closed");
            return false;
        }else {
            TextMessage textMessage = new TextMessage(message);
            try {
                synchronized (this) {
                    if(session==null||!session.isOpen()) {
                        logger.error("Session Is Null Or Closed");
                        return false;
                    }else{
                        session.sendMessage(textMessage);
                        return true;
                    }
                }
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    public abstract void onMessage(WebSocketSession session, String data) throws Exception;

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
