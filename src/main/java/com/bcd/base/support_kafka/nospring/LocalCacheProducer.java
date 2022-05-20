package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LocalCacheProducer<V> {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService pool_sendNotify = Executors.newSingleThreadExecutor();

    private final String topic;

    private final Producer<String, byte[]> producer;

    /**
     * @param producerProp 消费者属性
     * @param topic        消费的topic
     */
    public LocalCacheProducer(ProducerProp producerProp, String topic) {
        this.topic = topic;
        this.producer = ProducerFactory.newProducer(producerProp);
    }

    /**
     * 发送通知数据
     *
     * @param bytes
     */
    public void sendNotify(byte[] bytes) {
        pool_sendNotify.execute(() -> {
            producer.send(new ProducerRecord<>(topic, bytes));
        });
    }

}
