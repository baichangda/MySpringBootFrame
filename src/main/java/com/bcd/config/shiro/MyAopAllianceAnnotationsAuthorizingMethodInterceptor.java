package com.bcd.config.shiro;

import com.bcd.config.shiro.anno.ActionAnnotationMethodInterceptor;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;

/**
 * Created by Administrator on 2017/8/16.
 */
public class MyAopAllianceAnnotationsAuthorizingMethodInterceptor extends AopAllianceAnnotationsAuthorizingMethodInterceptor {
    public MyAopAllianceAnnotationsAuthorizingMethodInterceptor(){
        super();
        //自定义注解拦截器配置
        this.methodInterceptors.add(new ActionAnnotationMethodInterceptor(new SpringAnnotationResolver()));
    }
}
