package com.bcd.base.support_shiro;

import org.apache.shiro.aop.MethodInvocation;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 此处理器在shiro进过需要验证权限的方法时候会调用此处理器来判断是否跳过验证
 * 一般用于全局性的验证配置
 * 1、配置某类型的用户跳过所有权限认证
 * 2、配置某方法、请求跳过所有权限认证
 * <p>
 * 例如:
 * admin跳过所有权限验证
 */
public interface AuthorizationHandler {
    /**
     * 当前用户运行当前方法是否跳过验证
     * 此方法用于spring拦截器的权限认证
     * 用于:
     * MyAopAllianceAnnotationsAuthorizingMethodInterceptor
     * {@link org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor#assertAuthorized(MethodInvocation)}
     *
     * @param methodInvocation 当前需要验证权限的方法
     * @return
     */
    boolean skip(MethodInvocation methodInvocation);


    /**
     * 当前用户访问url的请求是否跳过验证
     * 此方法用于filter过滤器权限认证
     * 用于:
     * MyAuthorizationFilter
     * {@link org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter#isAccessAllowed(ServletRequest, ServletResponse, Object)}
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    boolean skip(ServletRequest request, ServletResponse response, Object mappedValue);
}
