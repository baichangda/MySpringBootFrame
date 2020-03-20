package com.bcd.config.cache;

import com.bcd.base.cache.ExpireConcurrentMapCache;
import com.bcd.base.cache.MySimpleKeyGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    /**
     * 定义一级缓存(在非redis情况)
     * 一级:过期的concurrentHashMap(过期时间5s);  key: myCache_1::${key}
     * @return
     */
    @ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
    @Bean("myCache")
    public Cache myCache(){
        return new ExpireConcurrentMapCache("myCache_1",5*1000L);
    }

    @Bean("mySimpleKeyGenerator")
    public MySimpleKeyGenerator mySimpleKeyGenerator(){
        return new MySimpleKeyGenerator();
    }
}
