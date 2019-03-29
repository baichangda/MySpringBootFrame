package com.bcd.base.websocket.data.nofity;

import java.util.function.Consumer;

public class NotifyConsumer {
    private NotifyEvent event;

    private String paramJson;

    private Consumer<String> consumer;

    public String getParamJson() {
        return paramJson;
    }

    public NotifyEvent getEvent() {
        return event;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }

    public NotifyConsumer(NotifyEvent event, String paramJson, Consumer<String> consumer) {
        this.event = event;
        this.paramJson=paramJson;
        this.consumer = consumer;
    }

}
