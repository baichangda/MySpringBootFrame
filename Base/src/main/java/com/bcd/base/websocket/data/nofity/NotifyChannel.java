package com.bcd.base.websocket.data.nofity;


import com.bcd.base.websocket.server.BaseWebSocket;
import org.springframework.web.socket.WebSocketSession;

public class NotifyChannel {
    private String sn;
    private NotifyEvent event;
    private WebSocketSession session;
    private BaseWebSocket webSocket;

    public NotifyChannel(String sn, NotifyEvent event, BaseWebSocket webSocket, WebSocketSession session) {
        this.sn = sn;
        this.event = event;
        this.webSocket = webSocket;
        this.session = session;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public NotifyEvent getEvent() {
        return event;
    }

    public void setEvent(NotifyEvent event) {
        this.event = event;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public BaseWebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(BaseWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    /**
     * 向通道的客户端发送数据
     *
     * @param data
     */
    public void sendMessage(String data) {
        this.webSocket.sendMessage(this.session, data);
    }
}
