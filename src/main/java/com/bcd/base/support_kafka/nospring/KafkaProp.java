package com.bcd.base.support_kafka.nospring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class KafkaProp {

    public ConsumerProp consumer;

    public ProducerProp producer;
}
