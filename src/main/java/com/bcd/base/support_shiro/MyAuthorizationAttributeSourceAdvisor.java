package com.bcd.base.support_shiro;

import com.bcd.base.support_shiro.anno.RequiresAction;
import com.bcd.base.support_shiro.anno.RequiresNotePermissions;
import com.bcd.base.support_shiro.anno.RequiresRequestMappingUrl;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/8/16.
 */
@SuppressWarnings("unchecked")
public class MyAuthorizationAttributeSourceAdvisor extends AuthorizationAttributeSourceAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(MyAuthorizationAttributeSourceAdvisor.class);

    private static final Class<? extends Annotation>[] AUTHZ_ANNOTATION_CLASSES =
            new Class[]{
                    RequiresPermissions.class, RequiresRoles.class,
                    RequiresUser.class, RequiresGuest.class, RequiresAuthentication.class,
                    RequiresAction.class, RequiresRequestMappingUrl.class, RequiresNotePermissions.class
            };

    /**
     * Create a new AuthorizationAttributeSourceAdvisor.
     */
    public MyAuthorizationAttributeSourceAdvisor(AuthorizationHandler authorizationHandler) {
        setAdvice(new MyAopAllianceAnnotationsAuthorizingMethodInterceptor(authorizationHandler));
    }

    /**
     * Returns <tt>true</tt> if the method or the class has any Shiro annotations, false otherwise.
     * The annotations inspected are:
     * <ul>
     * <li>{@link RequiresAuthentication RequiresAuthentication}</li>
     * <li>{@link RequiresUser RequiresUser}</li>
     * <li>{@link RequiresGuest RequiresGuest}</li>
     * <li>{@link RequiresRoles RequiresRoles}</li>
     * <li>{@link RequiresPermissions RequiresPermissions}</li>
     * </ul>
     *
     * @param method      the method to check for a Shiro annotation
     * @param targetClass the class potentially declaring Shiro annotations
     * @return <tt>true</tt> if the method has a Shiro annotation, false otherwise.
     * @see org.springframework.aop.MethodMatcher#matches(Method, Class)
     */
    public boolean matches(Method method, Class targetClass) {
        Method m = method;

        if (isAuthzAnnotationPresent(m)) {
            return true;
        }

        //The 'method' parameter could be from an interface that doesn't have the annotation.
        //Check to see if the implementation has it.
        if (targetClass != null) {
            try {
                m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                return isAuthzAnnotationPresent(m) || isAuthzAnnotationPresent(targetClass);
            } catch (NoSuchMethodException ignored) {
                //default return value is false.  If we can't find the method, then obviously
                //there is no annotation, so just use the default return value.
            }
        }

        return false;
    }

    private boolean isAuthzAnnotationPresent(Class<?> targetClazz) {
        for (Class<? extends Annotation> annClass : AUTHZ_ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(targetClazz, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthzAnnotationPresent(Method method) {
        for (Class<? extends Annotation> annClass : AUTHZ_ANNOTATION_CLASSES) {
            Annotation a = AnnotationUtils.findAnnotation(method, annClass);
            if (a != null) {
                return true;
            }
        }
        return false;
    }
}
