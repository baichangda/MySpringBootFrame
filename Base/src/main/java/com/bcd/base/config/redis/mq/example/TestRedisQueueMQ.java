package com.bcd.base.config.redis.mq.example;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.queue.RedisQueueMQ;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

//@Component
public class TestRedisQueueMQ extends RedisQueueMQ<String> implements SpringInitializable {
    public TestRedisQueueMQ(RedisConnectionFactory redisConnectionFactory) {
        super("test",redisConnectionFactory, ValueSerializerType.STRING);
    }

    @Override
    public void init(ContextRefreshedEvent event) {
        watch();
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
