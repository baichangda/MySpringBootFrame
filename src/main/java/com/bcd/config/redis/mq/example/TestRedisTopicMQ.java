package com.bcd.base.redis.mq.example;

import com.bcd.base.redis.mq.topic.RedisTopicMQ;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class TestRedisTopicMQ extends RedisTopicMQ{
    public TestRedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer) {
        super("test", redisMessageListenerContainer);
        watch();
    }

    @Override
    public void onMessage(Message data) {
        System.out.println(new String(data.getBody()));
    }
}
