package com.bcd.base.support_kafka.example.producer;

import com.bcd.base.util.DateZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Service
@SuppressWarnings("unchecked")
public class Producer implements ApplicationListener<ContextRefreshedEvent> {

    static Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate<byte[],byte[]> kafkaTemplate;

    public void sendMessage(byte[] key, byte[] msg) {
        kafkaTemplate.send("test", key, msg).whenComplete((sendResult, throwable) -> {
            if (throwable == null) {
                logger.info("send succeed");
            } else {
                logger.error("send failed", throwable);
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            sendMessage("test".getBytes(), DateZoneUtil.dateToString_second(new Date()).getBytes());
        }, 3, 3, TimeUnit.SECONDS);
    }
}