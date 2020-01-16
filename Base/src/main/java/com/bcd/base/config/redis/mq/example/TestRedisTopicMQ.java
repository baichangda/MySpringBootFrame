package com.bcd.base.config.redis.mq.example;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.config.redis.mq.ValueSerializerType;
import com.bcd.base.config.redis.mq.topic.RedisTopicMQ;
import com.bcd.base.util.JsonUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TestRedisTopicMQ extends RedisTopicMQ<TestBean> implements SpringInitializable {
    public TestRedisTopicMQ(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer, ValueSerializerType.JACKSON,"test");
    }

    @Override
    public void onMessage(TestBean data) {
        logger.info(JsonUtil.toJson(data));
    }

    @Override
    public void init(ContextRefreshedEvent event) {
        watch();
        TestBean testBean=new TestBean();
        testBean.setId(1);
        testBean.setName("呵呵");
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
//            send(testBean,"test");
//        },1,3, TimeUnit.SECONDS);
    }
}
