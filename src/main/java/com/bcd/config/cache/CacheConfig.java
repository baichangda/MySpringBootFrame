package com.bcd.config.cache;

import com.bcd.base.cache.MySimpleKeyGenerator;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    /**
     * 定义一级缓存(在非redis情况)
     * @return
     */
    @ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
    @Bean("myCache")
    public Cache myCache() {
        return new CaffeineCache("myCache_1",
            Caffeine.newBuilder()
                .expireAfterAccess(5,TimeUnit.SECONDS)
                .expireAfterWrite(5,TimeUnit.SECONDS)
                .softValues()
                .build()
        );
    }

    @Bean("mySimpleKeyGenerator")
    public MySimpleKeyGenerator mySimpleKeyGenerator() {
        return new MySimpleKeyGenerator();
    }
}
