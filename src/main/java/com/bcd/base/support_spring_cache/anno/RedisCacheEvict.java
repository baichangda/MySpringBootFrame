package com.bcd.base.support_spring_cache.anno;

import com.bcd.base.support_spring_cache.CacheConst;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.*;

@CacheEvict(cacheNames = CacheConst.REDIS_CACHE, keyGenerator = CacheConst.KEY_GENERATOR)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCacheEvict {


}
