package com.bcd.base.support_kafka.ext.datadriven;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 本类中所有的方法为了保证数据并发安全，都是在executor中执行的、包括
 * {@link #WorkHandler(String, WorkExecutor)}
 * {@link #init()}
 * {@link #destroy()}
 * {@link #onMessage(ConsumerRecord)}
 * 同时、为了保证线程安全性、建议所有异步操作交给executor执行、但是不要阻塞
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
