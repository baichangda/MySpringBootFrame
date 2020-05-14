package com.bcd.base.config.shiro.realm;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Collection;

public abstract class MyAuthorizingRealm extends AuthorizingRealm {
    /**
     * 清除用户的缓存权限数据,便于重新调用{@link #doGetAuthorizationInfo(PrincipalCollection)}重新获取权限
     * @param principalCollection
     */
    public void clearCachedAuthorizationInfo(PrincipalCollection principalCollection){
        super.clearCachedAuthorizationInfo(principalCollection);
    }

    /**
     * 清除用户的缓存登陆账户数据,便于重新调用{@link #doGetAuthenticationInfo(AuthenticationToken)}重新登陆校验
     * @param principalCollection
     */
    public void clearCachedAuthenticationInfo(PrincipalCollection principalCollection){
        super.clearCachedAuthenticationInfo(principalCollection);
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

    /**
     * 如果需要用到权限缓存,需要通过此方法设置其key
     * @param principals
     * @return
     */
    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return super.getAuthorizationCacheKey(principals);
    }

    /**
     * 如果需要用到登陆缓存,需要通过此方法设置其key
     * @param token
     * @return
     */
    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        return super.getAuthenticationCacheKey(token);
    }
}
