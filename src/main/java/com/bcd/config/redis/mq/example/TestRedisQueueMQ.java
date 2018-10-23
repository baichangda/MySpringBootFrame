package com.bcd.config.redis.mq.example;

import com.bcd.base.util.JsonUtil;
import com.bcd.config.redis.mq.queue.RedisQueueMQ;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestRedisQueueMQ extends RedisQueueMQ{
    public TestRedisQueueMQ(RedisConnectionFactory redisConnectionFactory) {
        super("test",redisConnectionFactory);
        watch();
    }

    @Bean(name = "testRedisQueueMQTemplate")
    private RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        return redisTemplate;
    }

    @Override
    public void onMessage(Object data) {
        System.out.println(JsonUtil.toJson(data));
    }
}
