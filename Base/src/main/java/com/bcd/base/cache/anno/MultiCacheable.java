package com.bcd.base.cache.anno;

import com.bcd.base.cache.CacheConst;
import org.springframework.cache.annotation.Cacheable;

import java.lang.annotation.*;

@Cacheable(cacheNames = {CacheConst.LOCAL_CACHE,CacheConst.REDIS_CACHE},keyGenerator = CacheConst.KEY_GENERATOR)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiCacheable {

}
