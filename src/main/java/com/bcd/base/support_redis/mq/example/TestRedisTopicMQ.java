package com.bcd.base.support_redis.mq.example;

import com.bcd.base.support_redis.mq.ValueSerializerType;
import com.bcd.base.support_redis.mq.topic.RedisTopicMQ;
import com.bcd.base.util.JsonUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Component
public class TestRedisTopicMQ extends RedisTopicMQ<TestBean[]> implements ApplicationListener<ContextRefreshedEvent> {
    public TestRedisTopicMQ(RedisConnectionFactory redisConnectionFactory) {
        super(redisConnectionFactory,1,1, ValueSerializerType.JACKSON, "test");
    }

    @Override
    public void onMessage(TestBean[] data) {
        logger.info(JsonUtil.toJson(data));
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        watch();
        TestBean testBean = new TestBean();
        testBean.setId(1);
        testBean.setName("呵呵");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            send(new TestBean[]{testBean, testBean}, "test");
        }, 1, 3, TimeUnit.SECONDS);
    }

}
