package com.bcd.sys;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;

import java.util.Collection;

public abstract class MyAuthorizingRealm extends AuthorizingRealm {
    /**
     * 清除当前用户的缓存权限数据,便于重新调用doGetAuthorizationInfo获取权限数据
     */
    public void clearCurrentUserCachedAuthorizationInfo(){
        clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

    /**
     * 获取当前用户的所有角色
     * @return
     */
    public Collection<String> getAllRoles(){
        AuthorizationInfo authorizationInfo= getAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
        return authorizationInfo.getRoles();
    }

    /**
     * 获取当前用户的所有权限
     * @return
     */
    public Collection<String> getAllPermissions(){
        AuthorizationInfo authorizationInfo= getAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
        return authorizationInfo.getStringPermissions();
    }
}
