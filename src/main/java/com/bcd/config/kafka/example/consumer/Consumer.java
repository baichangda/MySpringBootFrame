package com.bcd.config.kafka.example.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;


//@Service
public class Consumer {
    @KafkaListener(topics = "test")
    public void processMessage(ConsumerRecord<byte[],byte[]> consumerRecord) {
        System.out.println(consumerRecord);
    }
}