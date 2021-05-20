package com.bcd.base.cache.anno;

import com.bcd.base.cache.CacheConst;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.lang.annotation.*;

@CacheEvict(cacheNames = CacheConst.REDIS_CACHE,keyGenerator = CacheConst.KEY_GENERATOR)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCacheEvict {


}
