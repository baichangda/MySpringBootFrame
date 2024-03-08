package com.bcd.base.support_kafka.ext;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class ProducerFactory {

    final ThreadLocal<Producer<String, byte[]>> producers = new ThreadLocal<>();

    final ProducerProp producerProp;

    public ProducerFactory(ProducerProp producerProp) {
        this.producerProp=producerProp;
    }

    public static Producer<String, byte[]> newProducer(ProducerProp producerProp) {
        return new KafkaProducer<>(producerProp.toProperties());
    }

    /**
     * 获取producer并绑定到线程上
     */
    public Producer<String, byte[]> getProducerInThreadLocal() {
        Producer<String, byte[]> producer = producers.get();
        if (producer == null) {
            producer = newProducer(producerProp);
            producers.set(producer);
        }
        return producer;
    }
}
