package com.bcd.base.support_kafka.example.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Service
@SuppressWarnings("unchecked")
public class Producer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(byte[] key, byte[] msg) {
        kafkaTemplate.send("zs-feedback", key, msg).whenComplete((sendResult, throwable) -> {
            if (throwable == null) {
                System.out.println("onFailure!");
            } else {
                System.out.println("onSucceed!");
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            sendMessage("test".getBytes(), "bcd".getBytes());
        }, 3, 3, TimeUnit.SECONDS);
    }
}