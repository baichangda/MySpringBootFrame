package com.bcd.base.cache;

import org.springframework.beans.factory.annotation.Qualifier;
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
public class CacheConfig {
    @Bean("cacheRedisSerializer")
    public RedisSerializer<String> redisSerializer(){
        return new Jackson2JsonRedisSerializer(Object.class);
    }

    /**
     * 定义两级缓存
     * 一级:过期的concurrentHashMap(过期时间5s);  key: myCache_1::${key}
     * 二级:redis(过期时间15s);  key: myCache_2::${key}
     * @return
     */
    @Bean("myCache")
    public MultiLevelCache filterCache(RedisConnectionFactory factory,@Qualifier("cacheRedisSerializer")RedisSerializer<String> redisSerializer){
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
