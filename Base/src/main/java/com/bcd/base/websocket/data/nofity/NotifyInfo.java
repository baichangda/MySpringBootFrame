package com.bcd.base.websocket.data.nofity;

public class NotifyInfo {
    private String sn;
    private NotifyEvent event;

    public NotifyInfo(String sn, NotifyEvent event) {
        this.sn = sn;
        this.event = event;
    }

    public NotifyInfo() {
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
}
