package com.bcd.base.cache.anno;

import org.springframework.cache.annotation.CacheConfig;

import java.lang.annotation.*;

@CacheConfig(
        cacheNames = "myCache",
        keyGenerator = "mySimpleKeyGenerator"
)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MyCacheClass {


}
