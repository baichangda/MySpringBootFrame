package com.bcd.base.support_kafka.ext.datadriven;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 其中如下方法调用是由executor执行的
 * {@link #init()}
 * {@link #destroy()}
 * {@link #onMessage(ConsumerRecord)}
 * 为了保证线程安全性、建议所有异步操作交给executor执行
 */
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
