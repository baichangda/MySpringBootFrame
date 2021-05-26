package com.bcd.base.support_shiro.anno;

import com.bcd.base.support_shiro.ShiroMessageDefine;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/16.
 */
@SuppressWarnings("unchecked")
public class RequestMappingUrlAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {
    public RequestMappingUrlAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(new RequestMappingUrlAnnotationHandler(), resolver);
    }

    public RequestMappingUrlAnnotationMethodInterceptor() {
        super(new RequestMappingUrlAnnotationHandler());
    }

    @Override
    public void assertAuthorized(MethodInvocation mi) throws AuthorizationException {
        try {
            RequestMappingUrlAnnotationHandler handler = (RequestMappingUrlAnnotationHandler) getHandler();
            Method method = mi.getMethod();
            Class clazz = method.getDeclaringClass();
            RequestMapping classRequestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            RequestMapping methodRequestMapping = mi.getMethod().getAnnotation(RequestMapping.class);

            String[] classUrls = classRequestMapping.value();
            String[] methodUrls = methodRequestMapping.value();

            Set<String> permissionSet = new HashSet<>();
            Arrays.stream(classUrls).forEach(e1 -> {
                Arrays.stream(methodUrls).forEach(e2 -> {
                    permissionSet.add(e1 + e2);
                });
            });

            handler.setActionPermission(permissionSet);
            handler.assertAuthorized(getAnnotation(mi));
        } catch (AuthorizationException ae) {
            // Annotation handler doesn't know why it was called, so add the information here if possible.
            // Don't wrap the exception here since we don't want to mask the specific exception, such as
            // UnauthenticatedException etc.
            if (ae.getCause() == null) ae.initCause(ShiroMessageDefine.ERROR_SHIRO_AUTHORIZATION.toRuntimeException());
            throw ae;
        }
    }
}
