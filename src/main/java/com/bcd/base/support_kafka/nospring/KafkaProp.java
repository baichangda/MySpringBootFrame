package com.bcd.base.support_kafka.nospring;

import org.springframework.validation.annotation.Validated;

@Validated
public class KafkaProp {
    public ConsumerProp consumer;
    public ProducerProp producer;
}
