package com.bcd.base.config.redis.mq.example;

import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class TestRedisTopicMQ extends RedisTopicMQ<String>{
    public TestRedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.STRING,"test");
        watch();
    }

    @Override
    public void onMessage(String data) {
        logger.info(data);
    }
}
