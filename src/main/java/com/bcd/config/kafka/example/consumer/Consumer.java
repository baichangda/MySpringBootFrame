package com.bcd.config.kafka.example.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;


//@Service
public class Consumer {
    Logger logger= LoggerFactory.getLogger(Consumer.class);
    @KafkaListener(topics = "zs-feedback",groupId = "zs-device-platform1")
    public void processMessage(ConsumerRecord<byte[],byte[]> consumerRecord) {
        logger.info("receive message key[{}] value[{}]",new String(consumerRecord.key()),new String(consumerRecord.value()));
    }

}