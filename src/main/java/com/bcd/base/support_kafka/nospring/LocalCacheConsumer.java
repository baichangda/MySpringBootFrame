package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LocalCacheConsumer<V> extends AbstractConsumer {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentHashMap<String, V> map = new ConcurrentHashMap<>();

    private final ExecutorService pool_sendNotify = Executors.newSingleThreadExecutor();

    private final String topic;

    /**
     * @param consumerProp 消费者属性
     * @param groupId      消费组id
     * @param topic        消费的topic
     */
    public LocalCacheConsumer(ConsumerProp consumerProp, String groupId, String topic) {
        super(consumerProp, groupId, 1, 1000, true, true, topic);
        this.topic = topic;
    }

    @Override
    public void onMessage(ConsumerRecord<String, byte[]> consumerRecord) {
        onNotify(consumerRecord.value());
    }

    public V get(String k) {
        return map.get(k);
    }

    /**
     * 初始化
     */
    public abstract void init();

    /**
     * 当通知时候、完成更新map
     * @param bytes
     */
    public abstract void onNotify(byte[] bytes);

}
