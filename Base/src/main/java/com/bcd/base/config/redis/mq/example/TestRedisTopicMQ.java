package com.bcd.base.config.redis.mq.example;

import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class TestRedisTopicMQ extends RedisTopicMQ<String>{
    public TestRedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer) {
        super("test", redisMessageListenerContainer,String.class,ValueSerializer.STRING);
        watch();
    }

    @Override
    public void onMessage(String data) {
        logger.info(data);
    }
}
