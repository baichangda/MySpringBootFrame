package com.bcd.config.shiro;

import com.bcd.config.shiro.anno.ActionAnnotationMethodInterceptor;
import com.bcd.config.shiro.anno.UrlPermissionAnnotationMethodInterceptor;
import com.bcd.config.shiro.anno.UserInfoAnnotationMethodInterceptor;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.util.ShiroUtil;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
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
        this.methodInterceptors.add(new UserInfoAnnotationMethodInterceptor(new SpringAnnotationResolver()));
        this.methodInterceptors.add(new UrlPermissionAnnotationMethodInterceptor(new SpringAnnotationResolver()));
    }

    /**
     * 跳过admin的权限验证
     * @param methodInvocation
     * @throws AuthorizationException
     */
    @Override
    protected void assertAuthorized(MethodInvocation methodInvocation) throws AuthorizationException {
        UserBean userBean= ShiroUtil.getCurrentUser();
        if(userBean!=null && CommonConst.ADMIN_ID.equals(userBean.getId())){
            return;
        }
        super.assertAuthorized(methodInvocation);
    }
}
