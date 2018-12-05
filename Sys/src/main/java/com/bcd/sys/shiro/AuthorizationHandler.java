package com.bcd.sys.shiro;

import org.apache.shiro.aop.MethodInvocation;

/**
 * 此处理器在shiro进过需要验证权限的方法时候会调用此处理器来判断是否需要验证
 * 一般用于全局性的验证配置,例如:
 * admin跳过所有权限验证
 */
public interface CurrentUserAuthzHandler {
    /**
     * 当前用户运行当前方法是否需要检验权限
     * @param methodInvocation 当前需要验证权限的方法
     * @return
     */
    boolean isValidate(MethodInvocation methodInvocation);
}
