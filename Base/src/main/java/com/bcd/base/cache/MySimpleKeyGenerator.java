package com.bcd.base.cache;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

public class MySimpleKeyGenerator extends SimpleKeyGenerator{
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String methodName=method.getName();
        return generate(target,methodName,params);
    }

    public MySimpleKey generate(Object target, String methodName, Object... params) {
        return generate(target.getClass(),methodName,params);
    }

    public MySimpleKey generate(String className, String methodName, Object... params) {
        return new MySimpleKey(className,methodName,params);
    }

    public MySimpleKey generate(Class clazz, String methodName, Object... params) {
        return generate(clazz.getName(),methodName,params);
    }
}
