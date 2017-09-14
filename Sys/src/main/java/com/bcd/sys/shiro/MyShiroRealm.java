package com.bcd.sys.shiro;

import com.bcd.rdb.condition.BaseCondition;
import com.bcd.rdb.condition.impl.NumberCondition;
import com.bcd.rdb.condition.impl.StringCondition;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.UserService;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class MyShiroRealm extends AuthorizingRealm {
  
    private static final Logger logger = Logger.getLogger(MyShiroRealm.class);


    @Autowired
    private UserService userService;

    /**
     * 登录认证
     */  
    @Override  
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        UserBean user = userService.findOne(
                BaseCondition.and(
                    new StringCondition("username",token.getUsername(), StringCondition.Handler.EQUAL)
                )
        );

        if(user !=null){
            if(user.getStatus()==0){
                throw new DisabledAccountException();
            }
            // 若存在，将此用户存放到登录认证info中，无需自己做密码对比，Shiro会为我们进行密码对比校验
            SimpleAuthenticationInfo simpleAuthenticationInfo= new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
            simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(user.getUsername()));
            return simpleAuthenticationInfo;
        }
        return null;
    }  
  
    /**  
     * 权限认证（为当前登录的Subject授予角色和权限）  
     *  
     * 该方法的调用时机为需授权资源被访问时，并且每次访问需授权资源都会执行该方法中的逻辑，这表明本例中并未启用AuthorizationCache，  
     * 如果连续访问同一个URL（比如刷新），该方法不会被重复调用，Shiro有一个时间间隔（也就是cache时间，在ehcache-shiro.xml中配置），  
     * 超过这个时间间隔再刷新页面，该方法会被执行  
     *  
     * doGetAuthorizationInfo()是权限控制，  
     * 当访问到页面的时候，使用了相应的注解或者shiro标签才会执行此方法否则不会执行，  
     * 所以如果只是简单的身份认证没有权限的控制的话，那么这个方法可以不进行实现，直接返回null即可  
     */  
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        UserBean user = (UserBean) super.getAvailablePrincipal(principals);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if(user!=null){
            userService.findOne(new NumberCondition("id",user.getId(), NumberCondition.Handler.EQUAL));
            Set<String> roleSet=new HashSet<>();
            Set<String> permissionSet=new HashSet<>();
            user.getRoleBeanSet().forEach(role->{
                roleSet.add(role.getCode());
                role.getMenuBeanSet().forEach(menu->permissionSet.add(MenuBean.class.getSimpleName()+":"+menu.getId()));
                role.getPermissionBeanSet().forEach(permission->permissionSet.add(permission.getCode()));
            });
            info.setRoles(roleSet);
            info.setStringPermissions(permissionSet);
        }
         //返回null将会导致用户访问任何被拦截的请求时都会自动跳转到unauthorizedUrl指定的地址
        return info;
    }

    /**
     * 清除用户的权限数据,便于重新调用doGetAuthorizationInfo获取权限数据
     */
    public void clearAuthz(){
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