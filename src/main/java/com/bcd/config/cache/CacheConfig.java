package com.bcd.config.cache;

import com.bcd.base.cache.CacheConst;
import com.bcd.base.cache.MySimpleKeyGenerator;
import com.github.benmanes.caffeine.cache.Caffeine;
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
    @Bean(CacheConst.LOCAL_CACHE)
    public Cache localCache() {
        return new CaffeineCache(CacheConst.LOCAL_CACHE,
            Caffeine.newBuilder()
                .expireAfterWrite(5,TimeUnit.SECONDS)
                .softValues()
                .build()
        );
    }

    @Bean(CacheConst.KEY_GENERATOR)
    public MySimpleKeyGenerator mySimpleKeyGenerator() {
        return new MySimpleKeyGenerator();
    }
}
