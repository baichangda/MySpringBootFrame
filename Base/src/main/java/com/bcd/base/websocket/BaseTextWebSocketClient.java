package com.bcd.base.websocket;

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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class BaseTextWebSocketClient extends TextWebSocketHandler{

    LinkedBlockingQueue<String> blockingMessageQueue=new LinkedBlockingQueue<>();

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
        this.session=session;
        //进行阻塞数据发送
        String data;
        while((data=blockingMessageQueue.poll(500L, TimeUnit.MILLISECONDS))!=null){
            sendMessage(data);
        }
        super.afterConnectionEstablished(session);
    }

    public BaseTextWebSocketClient(String url) {
        this.url=url;
        StandardWebSocketClient client=new StandardWebSocketClient();
        manager=new WebSocketConnectionManager(client,this,url);
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
        logger.warn("webSocket connection closed,will restart it");
        this.session=null;
        this.manager.stop();
        this.manager.start();
        super.afterConnectionClosed(session, status);
    }

    public void sendMessage(String message){
        while(session==null||!session.isOpen()){
            blockingMessageQueue.add(message);
            logger.warn("RegisterWebSocketHandler session is null or closed,add message to queue");
        }
        TextMessage textMessage=new TextMessage(message);
        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public abstract void onMessage(WebSocketSession session, TextMessage message) throws Exception;
}
