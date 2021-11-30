package com.bcd.base.support_shiro.anno;

import com.bcd.base.util.StringUtil;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;

public class ActionAnnotationHandler extends AuthorizingAnnotationHandler {

    private final ThreadLocal<String> actionPermission = new ThreadLocal<>();

    public ActionAnnotationHandler() {
        super(RequiresAction.class);
    }

    public String getActionPermission() {
        return actionPermission.get();
    }

    public void setActionPermission(String permission) {
        actionPermission.set(permission);
    }

    @Override
    public void assertAuthorized(Annotation a) {
        Subject subject = getSubject();
        /**
         * 此处使用拼装出的字符串『ClassName : MethodName』
         * */
        String permissionString = getActionPermission();
        if (!StringUtil.isNullOrEmpty(permissionString)) {
            subject.checkPermission(permissionString);
        }
    }

}