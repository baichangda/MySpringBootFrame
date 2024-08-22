package com.bcd.base.support_kafka.ext.datadriven;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 本类中所有的方法为了保证数据并发安全，都是在executor中执行的、包括
 * {@link #WorkHandler(String, WorkExecutor)}
 * {@link #init()}
 * {@link #destroy()}
 * {@link #onMessage(ConsumerRecord)}
 * 同时、为了保证线程安全性、建议所有异步操作交给executor执行、但是不要阻塞
 *
 * 需要注意的是、此对象在内存中的数量和id数量有关、例如有10000个id的数据、则最多存在10000个对象
 * 所以不建议此对象子类的类变量定义过多过大、因为这样可能会导致内存消耗太大
 */
public abstract class WorkHandler {
    public final String id;
    public final WorkExecutor executor;
    public WorkHandler(String id, WorkExecutor executor) {
        this.id = id;
        this.executor = executor;
    }

    public abstract void onMessage(ConsumerRecord<String, byte[]> msg);
    public void init() {

    }
    public void destroy() {

    }
}
