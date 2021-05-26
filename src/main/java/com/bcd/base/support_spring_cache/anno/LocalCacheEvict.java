package com.bcd.base.cache.anno;

import com.bcd.base.cache.CacheConst;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.*;

@CacheEvict(cacheNames = CacheConst.LOCAL_CACHE, keyGenerator = CacheConst.KEY_GENERATOR)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocalCacheEvict {


}
