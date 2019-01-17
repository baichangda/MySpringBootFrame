package com.bcd.base.cache;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("mySimpleKeyGenerator")
public class MySimpleKeyGenerator extends SimpleKeyGenerator{
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String methodName=method.getName();
        return generate(target,methodName,params);
    }

    public MySimpleKey generate(Object target, String methodName, Object... params) {
        String className=target.getClass().getName();
        return new MySimpleKey(className,methodName,params);
    }

    public MySimpleKey generate(String className, String methodName, Object... params) {
        return new MySimpleKey(className,methodName,params);
    }

    public MySimpleKey generate(Class clazz, String methodName, Object... params) {
        String className=clazz.getName();
        return new MySimpleKey(className,methodName,params);
    }
}
