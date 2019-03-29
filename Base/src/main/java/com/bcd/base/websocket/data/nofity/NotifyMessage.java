package com.bcd.base.websocket.data.nofity;


import com.bcd.base.websocket.server.BaseWebSocket;

public class NotifyMessage {
    private String sn;
    private NotifyEvent event;
    private BaseWebSocket webSocket;

    public NotifyMessage(String sn, NotifyEvent event, BaseWebSocket webSocket) {
        this.sn = sn;
        this.event=event;
        this.webSocket = webSocket;
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

    public BaseWebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(BaseWebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
