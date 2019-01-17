package com.bcd.sys.rdb.shiro;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.shiro.ShiroMessageDefine;
import com.bcd.sys.MyAuthorizingRealm;
import com.bcd.sys.rdb.bean.UserBean;
import com.bcd.sys.rdb.define.CommonConst;
import com.bcd.sys.rdb.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class MyShiroRealm extends MyAuthorizingRealm {
  
    @Autowired
    private UserService userService;

    public MyShiroRealm() {
        if(CommonConst.IS_PASSWORD_ENCODED){
            HashedCredentialsMatcher hashedCredentialsMatcher= new HashedCredentialsMatcher(Md5Hash.ALGORITHM_NAME);
            hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
            setCredentialsMatcher(hashedCredentialsMatcher);
        }
    }

    /**
     * 登录认证
     */  
    @Override  
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken){
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        UserBean user = userService.findOne(
                Condition.and(
                        new StringCondition("username",token.getUsername(), StringCondition.Handler.EQUAL)
                )
        );
        if(user !=null){
            if(user.getStatus()==null||user.getStatus()==0){
                throw ShiroMessageDefine.ERROR_SHIRO_DISABLED_ACCOUNT.toRuntimeException();
            }
            // 若存在，将此用户存放到登录认证info中，无需自己做密码对比，Shiro会为我们进行密码对比校验
            SimpleAuthenticationInfo simpleAuthenticationInfo= new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
            if(CommonConst.IS_PASSWORD_ENCODED){
                simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(user.getUsername()));
            }
            return simpleAuthenticationInfo;
        }else{
            throw ShiroMessageDefine.ERROR_SHIRO_UNKNOWN_ACCOUNT.toRuntimeException();
        }
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
        Object userName= super.getAvailablePrincipal(principals);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if(userName!=null){
            UserBean user=userService.findOne(new StringCondition("username",userName));
            Set<String> roleSet=new HashSet<>();
            Set<String> permissionSet=new HashSet<>();
            info.setRoles(roleSet);
            info.setStringPermissions(permissionSet);
        }
         //返回null将会导致用户访问任何被拦截的请求时都会自动跳转到unauthorizedUrl指定的地址
        return info;
    }
}