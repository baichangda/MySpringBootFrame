package com.bcd.base.support_shiro.anno;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;
import java.util.Set;

@SuppressWarnings("unchecked")
public class RequestMappingUrlAnnotationHandler extends AuthorizingAnnotationHandler {

    private ThreadLocal<Set<String>> actionPermission = new ThreadLocal<>();

    public RequestMappingUrlAnnotationHandler() {
        super(RequiresRequestMappingUrl.class);
    }

    public Set<String> getActionPermission() {
        return actionPermission.get();
    }

    public void setActionPermission(Set<String> permission) {
        actionPermission.set(permission);
    }

    @Override
    public void assertAuthorized(Annotation a) {
        Subject subject = getSubject();
        Set<String> permissionSet = getActionPermission();
        if (permissionSet == null || permissionSet.isEmpty()) {
            return;
        }
        boolean flag = false;
        for (String e : permissionSet) {
            try {
                subject.checkPermission(e);
                flag = true;
                break;
            } catch (AuthorizationException ex) {
                break;
            }
        }
        if (!flag) {
            throw new AuthorizationException("No Url Permission!");
        }
    }
}