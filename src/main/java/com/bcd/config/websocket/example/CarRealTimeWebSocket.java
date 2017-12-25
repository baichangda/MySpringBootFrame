package com.bcd.config.plugins.websocket.example;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Administrator on 2017/6/22.
 */
@ServerEndpoint(value = "/carRealTimeWebSocket")
@Component
public class CarRealTimeWebSocket {
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private final static CopyOnWriteArraySet<CarRealTimeWebSocket> webSocketSet = new CopyOnWriteArraySet<CarRealTimeWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        webSocketSet.remove(this);  //从set中删除
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);

        //群发消息
        for (CarRealTimeWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 发生错误时使用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }


    /**
     * 发送消息
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
            this.session.getBasicRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     *
     */
    public static void sendInfo(String message) throws IOException {
        for (CarRealTimeWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    /**
     * 在线人数
     * @return
     */
    public static CopyOnWriteArraySet<CarRealTimeWebSocket> getOnLine(){
        return webSocketSet;
    }
}
