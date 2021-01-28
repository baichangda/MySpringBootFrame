package com.bcd.config.redis.cache;

import com.bcd.base.cache.LocalCache;
import com.bcd.base.cache.MultiLevelCache;
import com.bcd.base.config.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@SuppressWarnings("unchecked")
@EnableCaching
@Configuration
public class RedisCacheConfig {
    /**
     * 定义两级缓存
     * 一级:google guava缓存(过期时间5s);  key: myCache_1::${key}
     * 二级:redis(过期时间15s);  key: myCache_2::${key}
     * @return
     */
    @ConditionalOnClass(RedisConnectionFactory.class)
    @Bean("myCache")
    public Cache myCache(RedisConnectionFactory factory){
        RedisCacheManager redisCacheManager=new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(factory),
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisUtil.STRING_SERIALIZER))
                        .entryTtl(Duration.ofMillis(15*1000L))
                        .prefixCacheNameWith(RedisUtil.SYSTEM_REDIS_KEY_PRE)
        );
        MultiLevelCache cache= new MultiLevelCache("myCache",
                new LocalCache("myCache_1",5000L),
                redisCacheManager.getCache("myCache_2")
        );
        return cache;
    }




}
