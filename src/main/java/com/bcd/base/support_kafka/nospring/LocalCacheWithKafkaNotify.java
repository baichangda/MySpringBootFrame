package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public abstract class LocalCacheWithKafkaNotify<V> extends AbstractConsumer{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    private final ConcurrentHashMap<String,V> map=new ConcurrentHashMap<>();

    /**
     * @param consumerProp                 消费者属性
     * @param groupId                      消费组id
     * @param topics                       消费的topic
     */
    public LocalCacheWithKafkaNotify(ConsumerProp consumerProp, String groupId,String... topics) {
        super(consumerProp, groupId, 1, 1000, true, true, topics);
    }

    @Override
    public void onMessage(ConsumerRecord<String, byte[]> consumerRecord) {
        onNotify(consumerRecord.value());
    }

    public V get(String k){
        return map.get(k);
    }

    /**
     * 初始化
     */
    public abstract void init();

    /**
     * 有缓存变更通知时触发
     * @param bytes
     */
    public abstract void onNotify(byte[] bytes);
}
