package com.bcd.base.support_redis.cache;

import com.bcd.base.support_spring_cache.CacheConst;
import com.bcd.base.support_redis.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@SuppressWarnings("unchecked")
@Configuration
public class RedisCacheConfig {
    /**
     * redis缓存
     * @return
     */
    @ConditionalOnClass(RedisConnectionFactory.class)
    @Bean(CacheConst.REDIS_CACHE)
    public Cache redisCache(RedisConnectionFactory factory) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(factory),
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisUtil.STRING_SERIALIZER))
                        .entryTtl(Duration.ofMillis(5 * 1000L))
                        .prefixCacheNameWith(RedisUtil.SYSTEM_REDIS_KEY_PRE)
        );
        return redisCacheManager.getCache(CacheConst.REDIS_CACHE);
    }
}
