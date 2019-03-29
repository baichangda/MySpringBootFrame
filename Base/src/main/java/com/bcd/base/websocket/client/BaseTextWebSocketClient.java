package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public abstract class BaseTextWebSocketClient extends TextWebSocketHandler{

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
            onMessage(session, message);
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
            logger.error("WebSocket Connection Closed,Will Restart It");
            this.session = null;
            this.manager.stop();
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

    public abstract void onMessage(WebSocketSession session, TextMessage message) throws Exception;
}
