package com.bcd.base.websocket.data.nofity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.function.Consumer;

public class NotifyInfo {
    private String sn;
    private NotifyEvent event;
    private String paramJson;
    @JsonIgnore
    private Consumer<String> consumer;

    private String url;

    public NotifyInfo(String sn, NotifyEvent event, String paramJson, Consumer<String> consumer, String url) {
        this.sn = sn;
        this.event = event;
        this.paramJson = paramJson;
        this.consumer = consumer;
        this.url = url;
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

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
