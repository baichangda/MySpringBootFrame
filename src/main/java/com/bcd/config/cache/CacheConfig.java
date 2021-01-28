package com.bcd.config.cache;

import com.bcd.base.cache.LocalCache;
import com.bcd.base.cache.MySimpleKeyGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    /**
     * 定义一级缓存(在非redis情况)
     * 一级:google guava缓存(过期时间5s);  key: myCache_1::${key}
     * @return
     */
    @ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
    @Bean("myCache")
    public Cache myCache(){
        return new LocalCache("myCache_1",5L, TimeUnit.SECONDS);
    }

    @Bean("mySimpleKeyGenerator")
    public MySimpleKeyGenerator mySimpleKeyGenerator(){
        return new MySimpleKeyGenerator();
    }
}
