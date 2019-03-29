package com.bcd.base.websocket.server;

import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class BaseWebSocket {

    protected Logger logger= LoggerFactory.getLogger(getClass());

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private final static CopyOnWriteArraySet<BaseWebSocket> WEB_SOCKET_SET = new CopyOnWriteArraySet<>();

    protected Session session;

    /**
     * 连接打开时候触发
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WEB_SOCKET_SET.add(this);
    }

    /**
     * 连接关闭时候触发
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        WEB_SOCKET_SET.remove(this);
    }

    /**
     * 发生错误时候触发
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("Error",error);
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
                        this.session.getBasicRemote().sendText(message);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
