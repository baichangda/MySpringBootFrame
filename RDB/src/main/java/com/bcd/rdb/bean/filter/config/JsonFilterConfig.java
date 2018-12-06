package com.bcd.rdb.bean.filter.config;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JsonFilterConfig {
    /**
     * 定义过滤器缓存
     * @return
     */
    @Bean("jsonFilterCache")
    public Cache filterCache(){
        Cache cache= new ConcurrentMapCache("jsonFilterCache",true);
        return cache;
    }
}
