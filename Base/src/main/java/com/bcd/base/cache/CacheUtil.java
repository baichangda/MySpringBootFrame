package com.bcd.base.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;


@Component
public class CacheUtil {
    public static Cache cache;
    public static MySimpleKeyGenerator keyGenerator;
    @Autowired
    @Qualifier("myCache")
    public void setCache(Cache cache) {
        CacheUtil.cache=cache;
    }

    @Autowired
    public void setKeyGenerator(MySimpleKeyGenerator keyGenerator) {
        CacheUtil.keyGenerator=keyGenerator;
    }

    public static MySimpleKey getCacheKey(Class clazz, String methodName,Object ... args){
        return keyGenerator.generate(clazz,methodName,args);
    }

    public static MySimpleKey getCacheKey(String className, String methodName,Object ... args){
        return keyGenerator.generate(className,methodName,args);
    }

    public static MySimpleKey getCacheKey(Object obj, String methodName,Object ... args){
        return keyGenerator.generate(obj,methodName,args);
    }
}
