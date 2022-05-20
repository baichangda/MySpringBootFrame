package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class ProducerUtil {

    static ThreadLocal<Producer<String, byte[]>> producers = new ThreadLocal<>();

    static ProducerProp producerProp;

    public void setKafkaProperties(ProducerProp producerProp) {
        ProducerUtil.producerProp = producerProp;
    }

    public static Producer<String, byte[]> getProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerProp.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.ACKS_CONFIG, producerProp.acks);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, producerProp.batchSize);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, producerProp.compressionType);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerProp.bufferMemory);
        return new KafkaProducer<>(props);
    }

    /**
     * 获取producer并绑定到线程上
     */
    public static Producer<String, byte[]> getProducerInThreadLocal() {
        Producer<String, byte[]> producer = producers.get();
        if (producer == null) {
            producer = ProducerUtil.getProducer();
            producers.set(producer);
        }
        return producer;
    }
}
