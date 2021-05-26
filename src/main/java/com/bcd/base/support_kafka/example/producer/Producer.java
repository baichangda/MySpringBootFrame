package com.bcd.base.support_kafka.example.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Service
@SuppressWarnings("unchecked")
public class Producer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(byte[] key, byte[] msg) {
        ListenableFuture<SendResult<byte[], byte[]>> listenableFuture = kafkaTemplate.send("zs-feedback", key, msg);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<byte[], byte[]>>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("onFailure!");
            }

            @Override
            public void onSuccess(SendResult<byte[], byte[]> result) {
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