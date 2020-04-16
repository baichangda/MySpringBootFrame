package com.bcd.base.config.shiro.realm;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Collection;

public abstract class MyAuthorizingRealm extends AuthorizingRealm {
    /**
     * 清除用户的缓存权限数据,便于重新调用doGetAuthorizationInfo获取权限数据
     * @param principalCollection
     */
    public void clearCurrentUserCachedAuthorizationInfo(PrincipalCollection principalCollection){
        clearCachedAuthorizationInfo(principalCollection);
    }

    /**
     * 获取用户的所有角色
     * @param principalCollection
     * @return
     */
    public Collection<String> getAllRoles(PrincipalCollection principalCollection){
        AuthorizationInfo authorizationInfo= getAuthorizationInfo(principalCollection);
        return authorizationInfo.getRoles();
    }

    /**
     * 获取用户的所有权限
     * @param principalCollection
     * @return
     */
    public Collection<String> getAllPermissions(PrincipalCollection principalCollection){
        AuthorizationInfo authorizationInfo= getAuthorizationInfo(principalCollection);
        return authorizationInfo.getStringPermissions();
    }
}
