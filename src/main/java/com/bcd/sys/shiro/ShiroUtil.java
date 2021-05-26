package com.bcd.sys.shiro;

import com.bcd.base.support_shiro.realm.MyAuthorizingRealm;
import com.bcd.sys.bean.UserBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/26.
 */
@SuppressWarnings("unchecked")
public class ShiroUtil {
    /**
     * 清除当前登录用户缓存权限信息
     */
    public static void clearCurrentUserCachedAuthorizationInfo() {
        RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        Map<String, Realm> nameToRealm = rsm.getRealms().stream().collect(Collectors.toMap(Realm::getName, e -> e));
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        for (String realmName : principalCollection.getRealmNames()) {
            Optional.ofNullable(nameToRealm.get(realmName)).ifPresent(e -> ((MyAuthorizingRealm) e).clearCachedAuthorizationInfo(principalCollection));
        }
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    public static UserBean getCurrentUser() {
        UserBean user;
        try {
            user = (UserBean) SecurityUtils.getSubject().getSession().getAttribute("user");
        } catch (UnavailableSecurityManagerException e) {
            user = null;
        }
        return user;
    }

    /**
     * 获取当前登录用户的所有角色
     *
     * @return
     */
    public static Set<String> getCurrentUserRoles() {
        Set<String> roleSet = new HashSet<>();
        RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        Map<String, Realm> nameToRealm = rsm.getRealms().stream().collect(Collectors.toMap(Realm::getName, e -> e));
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        for (String realmName : principalCollection.getRealmNames()) {
            Optional.ofNullable(nameToRealm.get(realmName)).ifPresent(e -> roleSet.addAll(((MyAuthorizingRealm) e).getAllRoles(principalCollection)));
        }
        return roleSet;
    }

    /**
     * 获取当前登录用户的所有权限
     *
     * @return
     */
    public static Collection<String> getCurrentUserPermissions() {
        Set<String> permissionSet = new HashSet<>();
        RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        Map<String, Realm> nameToRealm = rsm.getRealms().stream().collect(Collectors.toMap(Realm::getName, e -> e));
        PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
        for (String realmName : principalCollection.getRealmNames()) {
            Optional.ofNullable(nameToRealm.get(realmName)).ifPresent(e -> permissionSet.addAll(((MyAuthorizingRealm) e).getAllPermissions(principalCollection)));
        }
        return permissionSet;
    }

}
