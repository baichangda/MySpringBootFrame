package com.bcd.base.config.redis.schedule.aop;

import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 定时任务注解Aop配置的RedisConnectionFactory 提供者
 */
public interface ScheduleAopRedisConnectionFactorySupplier {
    RedisConnectionFactory getRedisConnectionFactory();
}
