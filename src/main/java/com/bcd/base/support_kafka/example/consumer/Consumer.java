package com.bcd.base.support_kafka.example.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


//@Service
public class Consumer {
    Logger logger = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "test")
    public void processMessage(ConsumerRecord<byte[], byte[]> consumerRecord) {
        logger.info("receive message key[{}] value[{}]", new String(consumerRecord.key()), new String(consumerRecord.value()));
    }

}