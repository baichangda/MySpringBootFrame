package com.bcd.base.support_kafka.ext.datadriven;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public abstract class WorkHandler {
    public final String id;
    public final WorkExecutor executor;
    public WorkHandler(String id, WorkExecutor executor) {
        this.id = id;
        this.executor = executor;
    }
    public abstract void onMessage(ConsumerRecord<String, byte[]> msg);
    public void init() {}
    public void destroy() {

    }
}
