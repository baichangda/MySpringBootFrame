package com.bcd.config.plugins.shiro.anno;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

public class ActionAnnotationHandler extends AuthorizingAnnotationHandler {

    public ActionAnnotationHandler() {
        super(RequiresAction.class);
        // TODO Auto-generated constructor stub
    }

    private ThreadLocal<String>  actionPermission = new ThreadLocal<>();
    public void setActionPermission(String permission){
        actionPermission.set(permission);
    }
    public String getActionPermission(){
        return actionPermission.get();
    }

    @Override
    public void assertAuthorized(Annotation a) throws AuthorizationException {
        // TODO Auto-generated method stub

        Subject subject = getSubject();
        /**
         * 此处使用拼装出的字符串『ClassName : MethodName』
         * */
        String permissionString=getActionPermission();
        if(!StringUtils.isEmpty(permissionString)){
            subject.checkPermission(permissionString);
            return;
        }
    }

}