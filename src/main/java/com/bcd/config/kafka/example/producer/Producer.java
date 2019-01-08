package com.bcd.config.kafka.example.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

//@Service
public class Producer {
    @Autowired
    private KafkaTemplate<byte[],byte[]> kafkaTemplate;

    public void sendMessage(byte[] key,byte[] msg){
        ListenableFuture<SendResult<byte[],byte[]>> listenableFuture= kafkaTemplate.send("test",key, msg);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<byte[],byte[]>>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("onFailure!");
            }

            @Override
            public void onSuccess(SendResult<byte[],byte[]> result) {
                System.out.println("onSucceed!");
            }
        });
    }
}