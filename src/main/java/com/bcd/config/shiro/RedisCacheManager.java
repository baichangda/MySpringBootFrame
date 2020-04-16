package com.bcd.config.shiro;

import com.bcd.base.config.redis.RedisUtil;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@SuppressWarnings("unchecked")
public class RedisCacheManager extends AbstractCacheManager{
    RedisTemplate<String, String> redisTemplate;

    public RedisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(redisConnectionFactory);
        this.redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        this.redisTemplate.setHashKeySerializer(RedisUtil.JDK_SERIALIZATION_SERIALIZER);
        this.redisTemplate.setValueSerializer(RedisUtil.STRING_SERIALIZER);
        this.redisTemplate.setHashValueSerializer(RedisUtil.JDK_SERIALIZATION_SERIALIZER);
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    protected Cache createCache(String s) throws CacheException {
        return new RedisCache(redisTemplate,s);
    }

}
