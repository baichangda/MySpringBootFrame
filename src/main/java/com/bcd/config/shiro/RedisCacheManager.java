package com.bcd.config.shiro;

import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.shiro.cache.ShiroRedisCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class RedisCacheManager extends AbstractCacheManager {
    RedisTemplate<String, String> redisTemplate;

    long localExpired;

    TimeUnit unit;

    public RedisCacheManager(RedisConnectionFactory redisConnectionFactory, long localExpired, TimeUnit unit) {
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(redisConnectionFactory);
        this.redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        this.redisTemplate.setHashKeySerializer(RedisUtil.JDK_SERIALIZATION_SERIALIZER);
        this.redisTemplate.setValueSerializer(RedisUtil.STRING_SERIALIZER);
        this.redisTemplate.setHashValueSerializer(RedisUtil.JDK_SERIALIZATION_SERIALIZER);
        this.redisTemplate.afterPropertiesSet();
        this.localExpired = localExpired;
        this.unit = unit;
    }

    @Override
    protected Cache createCache(String s) throws CacheException {
        return new ShiroRedisCache(redisTemplate, s, localExpired, unit);
    }

}
