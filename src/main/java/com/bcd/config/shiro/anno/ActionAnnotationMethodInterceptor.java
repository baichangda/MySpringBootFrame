package com.bcd.config.shiro.anno;

import com.bcd.base.config.shiro.ShiroMessageDefine;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;

/**
 * Created by Administrator on 2017/8/16.
 */
public class ActionAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor{
    public ActionAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(new ActionAnnotationHandler(),resolver);
    }
    public ActionAnnotationMethodInterceptor() {
        super(new ActionAnnotationHandler());
    }

    @Override
    public void assertAuthorized(MethodInvocation mi){
        try {
            ActionAnnotationHandler handler= (ActionAnnotationHandler)getHandler();
            StringBuilder sb = new StringBuilder();
            sb.append(mi.getThis().getClass().getName());
            sb.append(":");
            sb.append(mi.getMethod().getName());
            handler.setActionPermission(sb.toString());
            handler.assertAuthorized(getAnnotation(mi));
        }
        catch(AuthorizationException ae) {
            // Annotation handler doesn't know why it was called, so add the information here if possible.
            // Don't wrap the exception here since we don't want to mask the specific exception, such as
            // UnauthenticatedException etc.
            if (ae.getCause() == null) ae.initCause(ShiroMessageDefine.ERROR_SHIRO_AUTHORIZATION.toRuntimeException());
            throw ae;
        }
    }
}
