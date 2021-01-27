package com.bcd.base.config.redis.mq.example;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.queue.RedisQueueMQ;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Component
public class TestRedisQueueMQ extends RedisQueueMQ<String> implements SpringInitializable {
    public TestRedisQueueMQ(RedisConnectionFactory redisConnectionFactory) {
        super("a",redisConnectionFactory, ValueSerializerType.STRING);
    }

    @Override
    public void init(ContextRefreshedEvent event) {
        watch();
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
//            sendBatch(Arrays.asList("1","2"));
//        },1,5, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void onMessage(String data) {
        logger.info(data);
    }
}
