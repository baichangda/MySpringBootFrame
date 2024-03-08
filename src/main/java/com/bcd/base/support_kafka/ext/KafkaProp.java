package com.bcd.base.support_kafka.ext;

import org.springframework.validation.annotation.Validated;

@Validated
public class KafkaProp {
    public ConsumerProp consumer;
    public ProducerProp producer;

    public ConsumerProp getConsumer() {
        return this.consumer;
    }

    public void setConsumer(ConsumerProp consumer) {
        this.consumer = consumer;
    }

    public ProducerProp getProducer() {
        return this.producer;
    }

    public void setProducer(ProducerProp producer) {
        this.producer = producer;
    }
}
