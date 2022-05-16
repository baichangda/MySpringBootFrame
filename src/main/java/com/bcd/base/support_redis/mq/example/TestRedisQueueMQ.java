package com.bcd.base.support_redis.mq.example;

import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.queue.RedisQueueMQ;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;

//@Component
public class TestRedisQueueMQ extends RedisQueueMQ<String> implements ApplicationListener<ContextRefreshedEvent> {
    public TestRedisQueueMQ(RedisConnectionFactory redisConnectionFactory) {
        super("a", redisConnectionFactory, ValueSerializerType.STRING,1,1);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        init();
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
//            sendBatch(Arrays.asList("1","2"));
//        },1,5, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String data) {
        logger.info(data);
    }
}
