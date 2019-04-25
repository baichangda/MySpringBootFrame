package com.bcd.base.websocket.data.nofity;


import com.bcd.base.websocket.server.BaseWebSocket;

public class SubscribeInfo {
    private String sn;
    private NotifyEvent event;
    private BaseWebSocket.ServiceInstance serviceInstance;

    public SubscribeInfo(String sn, NotifyEvent event, BaseWebSocket.ServiceInstance serviceInstance) {
        this.sn = sn;
        this.event=event;
        this.serviceInstance = serviceInstance;
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

    public BaseWebSocket.ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(BaseWebSocket.ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
