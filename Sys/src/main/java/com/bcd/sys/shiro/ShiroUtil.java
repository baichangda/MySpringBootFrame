package com.bcd.sys.shiro;

import com.bcd.sys.MyAuthorizingRealm;
import com.bcd.sys.UserDataAccess;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.RealmSecurityManager;

import java.util.Collection;

/**
 * Created by Administrator on 2017/4/26.
 */
@SuppressWarnings("unchecked")
public class ShiroUtil {
    /**
     * 清除当前登录用户缓存权限信息
     */
    public static void clearCurrentUserCachedAuthorizationInfo(){
        RealmSecurityManager rsm= (RealmSecurityManager) SecurityUtils.getSecurityManager();
        MyAuthorizingRealm realm=  (MyAuthorizingRealm)rsm.getRealms().iterator().next();
        realm.clearCurrentUserCachedAuthorizationInfo();
    }

    /**
     * 获取当前登录用户
     * @return
     */
    public static <T extends UserDataAccess>T getCurrentUser(){
        T user;
        try{
            user=(T)SecurityUtils.getSubject().getSession().getAttribute("user");
        }catch (UnavailableSecurityManagerException e){
            user=null;
        }
        return user;
    }

    /**
     * 获取当前登录用户的所有角色
     * @return
     */
    public static Collection<String> getCurrentUserRoles(){
        RealmSecurityManager rsm= (RealmSecurityManager) SecurityUtils.getSecurityManager();
        MyAuthorizingRealm realm=  (MyAuthorizingRealm)rsm.getRealms().iterator().next();
        return realm.getAllRoles();
    }

    /**
     * 获取当前登录用户的所有权限
     * @return
     */
    public static Collection<String> getCurrentUserPermissions(){
        RealmSecurityManager rsm= (RealmSecurityManager) SecurityUtils.getSecurityManager();
        MyAuthorizingRealm realm=  (MyAuthorizingRealm)rsm.getRealms().iterator().next();
        return realm.getAllPermissions();
    }

}
