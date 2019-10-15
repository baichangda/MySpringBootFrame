package com.bcd.base.config.redis.schedule.aop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("my.enableScheduleFailedAnnotation")
public class DefaultScheduleAopRedisConnectionFactorySupplier implements ScheduleAopRedisConnectionFactorySupplier{
    private RedisConnectionFactory redisConnectionFactory;

    public DefaultScheduleAopRedisConnectionFactorySupplier(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public RedisConnectionFactory getRedisConnectionFactory() {
        return redisConnectionFactory;
    }
}
