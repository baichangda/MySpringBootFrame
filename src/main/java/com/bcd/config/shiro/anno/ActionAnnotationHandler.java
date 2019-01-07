package com.bcd.config.shiro.anno;

import com.bcd.base.config.shiro.anno.RequiresAction;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

public class ActionAnnotationHandler extends AuthorizingAnnotationHandler {

    public ActionAnnotationHandler() {
        super(RequiresAction.class);
    }

    private ThreadLocal<String>  actionPermission = new ThreadLocal<>();
    public void setActionPermission(String permission){
        actionPermission.set(permission);
    }
    public String getActionPermission(){
        return actionPermission.get();
    }

    @Override
    public void assertAuthorized(Annotation a){
        Subject subject = getSubject();
        /**
         * 此处使用拼装出的字符串『ClassName : MethodName』
         * */
        String permissionString=getActionPermission();
        if(!StringUtils.isEmpty(permissionString)){
            subject.checkPermission(permissionString);
        }
    }

}