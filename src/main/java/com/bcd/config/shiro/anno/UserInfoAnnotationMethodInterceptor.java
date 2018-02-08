package com.bcd.config.shiro.anno;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;

/**
 * Created by Administrator on 2017/8/16.
 */
public class UserInfoAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor{
    public UserInfoAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(new UserInfoAnnotationHandler(),resolver);
    }
    public UserInfoAnnotationMethodInterceptor() {
        super(new UserInfoAnnotationHandler());
    }
}
