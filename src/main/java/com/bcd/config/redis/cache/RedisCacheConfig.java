package com.bcd.config.redis.cache;

import com.bcd.base.cache.ExpireConcurrentMapCache;
import com.bcd.base.cache.MultiLevelCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
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
    @Bean("redisCacheSerializer")
    public RedisSerializer<String> redisSerializer(){
        return new Jackson2JsonRedisSerializer(Object.class);
    }

    /**
     * 定义两级缓存
     * 一级:过期的concurrentHashMap(过期时间5s);  key: myCache_1::${key}
     * 二级:redis(过期时间15s);  key: myCache_2::${key}
     * @return
     */
    @ConditionalOnClass(RedisConnectionFactory.class)
    @Bean("myCache")
    public Cache myCache(RedisConnectionFactory factory,@Qualifier("redisCacheSerializer")RedisSerializer<String> redisSerializer){
        RedisCacheManager redisCacheManager=new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(factory),
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                        .entryTtl(Duration.ofMillis(15*1000L))
        );
        MultiLevelCache cache= new MultiLevelCache("myCache",
                new ExpireConcurrentMapCache("myCache_1",5*1000L),
                redisCacheManager.getCache("myCache_2")
        );
        return cache;
    }




}
