package com.bcd.sys.mongodb.shiro;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.shiro.ShiroMessageDefine;
import com.bcd.sys.MyAuthorizingRealm;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.mongodb.bean.UserBean;
import com.bcd.sys.mongodb.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

//@Component
public class MyShiroRealm extends MyAuthorizingRealm{
    public MyShiroRealm() {
        if(CommonConst.IS_PASSWORD_ENCODED){
            HashedCredentialsMatcher hashedCredentialsMatcher= new HashedCredentialsMatcher(Md5Hash.ALGORITHM_NAME);
            hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
            setCredentialsMatcher(hashedCredentialsMatcher);
        }
    }

    @Autowired
    UserService userService;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        Object userName= super.getAvailablePrincipal(principalCollection);
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

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
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
}
