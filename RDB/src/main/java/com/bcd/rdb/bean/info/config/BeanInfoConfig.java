package com.bcd.rdb.bean.info.config;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BeanInfoConfig {
    /**
     * 定义实体类信息缓存
     * @return
     */
    @Bean("beanInfoCache")
    public Cache filterCache(){
        Cache cache=new ConcurrentMapCache("beanInfoCache",true);
        return cache;
    }
}
