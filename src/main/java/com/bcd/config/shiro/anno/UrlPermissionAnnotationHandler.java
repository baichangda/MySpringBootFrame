package com.bcd.config.shiro.anno;

import com.bcd.base.config.shiro.anno.RequiresUrlPermission;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;
import java.util.Set;
@SuppressWarnings("unchecked")
public class UrlPermissionAnnotationHandler extends AuthorizingAnnotationHandler {

    public UrlPermissionAnnotationHandler() {
        super(RequiresUrlPermission.class);
    }

    private ThreadLocal<Set<String>>  actionPermission = new ThreadLocal<>();

    public void setActionPermission(Set<String> permission){
        actionPermission.set(permission);
    }
    public Set<String> getActionPermission(){
        return actionPermission.get();
    }

    @Override
    public void assertAuthorized(Annotation a){
        Subject subject = getSubject();
        Set<String> permissionSet=getActionPermission();
        if(permissionSet==null||permissionSet.isEmpty()){
            return;
        }
        boolean flag=false;
        for (String e : permissionSet) {
            try {
                subject.checkPermission(e);
                flag=true;
                break;
            }catch (AuthorizationException ex){
                break;
            }
        }
        if(!flag){
            throw new AuthorizationException("No Url Permission!");
        }
    }
}