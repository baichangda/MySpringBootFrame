package com.bcd.base.support_redis.mq.example;

import com.bcd.base.support_spring_init.SpringInitializable;
import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.queue.RedisQueueMQ;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;

//@Component
public class TestRedisQueueMQ extends RedisQueueMQ<String> implements SpringInitializable {
    public TestRedisQueueMQ(RedisConnectionFactory redisConnectionFactory) {
        super("a", redisConnectionFactory, ValueSerializerType.STRING);
    }

    @Override
    public void init(ContextRefreshedEvent event) {
        watch();
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
//            sendBatch(Arrays.asList("1","2"));
//        },1,5, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String data) {
        logger.info(data);
    }
}
