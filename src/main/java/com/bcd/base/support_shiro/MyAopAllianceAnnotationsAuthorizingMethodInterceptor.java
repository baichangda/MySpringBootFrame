package com.bcd.base.support_shiro;

import com.bcd.base.support_shiro.anno.ActionAnnotationMethodInterceptor;
import com.bcd.base.support_shiro.anno.NotePermissionAnnotationMethodInterceptor;
import com.bcd.base.support_shiro.anno.RequestMappingUrlAnnotationMethodInterceptor;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;

/**
 * Created by Administrator on 2017/8/16.
 */
public class MyAopAllianceAnnotationsAuthorizingMethodInterceptor extends AopAllianceAnnotationsAuthorizingMethodInterceptor {
    private AuthorizationHandler authorizationHandler;

    public MyAopAllianceAnnotationsAuthorizingMethodInterceptor(AuthorizationHandler authorizationHandler) {
        super();
        this.authorizationHandler = authorizationHandler;
        //自定义注解拦截器配置
        this.methodInterceptors.add(new ActionAnnotationMethodInterceptor(new SpringAnnotationResolver()));
        this.methodInterceptors.add(new RequestMappingUrlAnnotationMethodInterceptor(new SpringAnnotationResolver()));
        this.methodInterceptors.add(new NotePermissionAnnotationMethodInterceptor(new SpringAnnotationResolver()));
    }

    /**
     * 跳过admin的权限验证
     *
     * @param methodInvocation
     * @throws AuthorizationException
     */
    @Override
    protected void assertAuthorized(MethodInvocation methodInvocation) {
        if (authorizationHandler.skip(methodInvocation)) {
            return;
        }
        super.assertAuthorized(methodInvocation);
    }
}
