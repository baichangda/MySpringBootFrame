package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public abstract class BaseWebSocket extends TextWebSocketHandler implements WebSocketConfigurer{

    /**
     * 客户端连接集合
     */
    protected final CopyOnWriteArraySet<WebSocketSession> clientSessionSet=new CopyOnWriteArraySet<>();

    protected String url;

    protected Logger logger= LoggerFactory.getLogger(getClass());

    public BaseWebSocket(String url) {
        this.url = url;

    }

    public CopyOnWriteArraySet<WebSocketSession> getClientSessionSet() {
        return clientSessionSet;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 发送信息
     * @param session
     * @param message
     */
    public boolean sendMessage(WebSocketSession session,String message){
        try {
            if(session==null||!session.isOpen()){
                return false;
            }else {
                List<String> subList;
                if(supportsPartialMessages()) {
                    subList = new LinkedList<>();
                    int len = message.length();
                    int index = 0;
                    while (true) {
                        int start = index;
                        int end = index + 1024;
                        if (end >= len) {
                            break;
                        }
                        String sub = message.substring(start, end);
                        subList.add(sub);
                        index = end;
                    }
                    subList.add(message.substring(index, len));
                }else{
                    subList= Arrays.asList(message);
                }
                synchronized (session) {
                    if(!session.isOpen()){
                        return false;
                    }else {
                        int subSize=subList.size();
                        for(int i=0;i<=subSize-2;i++){
                            session.sendMessage(new TextMessage(subList.get(i),false));
                        }
                        session.sendMessage(new TextMessage(subList.get(subSize-1),true));
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this,url).setAllowedOrigins("*");
    }

    /**
     * 连接打开时候触发
     * @param session
     */
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        clientSessionSet.add(session);
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception{
        super.handleMessage(session,message);
    }


    /**
     * 连接关闭时候触发
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
        clientSessionSet.remove(session);
    }

    /**
     * 发生错误时候触发
     * @param session
     * @param exception
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception{
        ExceptionUtil.printException(exception);
    }




    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
