package com.bcd.sys.shiro;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.shiro.cache.RedisCache;
import com.bcd.base.config.shiro.realm.MyAuthorizingRealm;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.service.PermissionService;
import com.bcd.sys.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class UsernamePasswordRealm extends MyAuthorizingRealm {

    @Autowired
    PermissionService permissionService;
    @Autowired
    private UserService userService;

    public UsernamePasswordRealm(RedisConnectionFactory redisConnectionFactory) {
        if (CommonConst.IS_PASSWORD_ENCODED) {
            HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher(Md5Hash.ALGORITHM_NAME);
            hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
            setCredentialsMatcher(hashedCredentialsMatcher);
        }
        setAuthenticationTokenClass(UsernamePasswordToken.class);
        //关闭登陆缓存
        setAuthenticationCachingEnabled(false);
        //开启权限缓存
        setAuthorizationCachingEnabled(true);
        //设置缓存管理器
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashKeySerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setValueSerializer(RedisUtil.STRING_SERIALIZER);
        redisTemplate.setHashValueSerializer(RedisUtil.newJackson2JsonRedisSerializer(SimpleAuthorizationInfo.class));
        redisTemplate.afterPropertiesSet();
        setCacheManager(new CacheManager() {
            @Override
            public <K, V> Cache<K, V> getCache(String s) throws CacheException {
                if (s.equals(getAuthorizationCacheName())) {
                    return new RedisCache<>(redisTemplate, s, 5, TimeUnit.SECONDS);
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        UserBean user = userService.findOne(
                Condition.and(
                        new StringCondition("username", token.getUsername(), StringCondition.Handler.EQUAL)
                )
        );
        if (user != null) {
            if (user.getStatus() == null || user.getStatus() == 0) {
                throw new DisabledAccountException();
            }
            // 若存在，将此用户存放到登录认证info中，无需自己做密码对比，Shiro会为我们进行密码对比校验
            SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
            if (CommonConst.IS_PASSWORD_ENCODED) {
                simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(user.getUsername()));
            }
            return simpleAuthenticationInfo;
        } else {
            throw new UnknownAccountException();
        }
    }

    /**
     * 权限认证（为当前登录的Subject授予角色和权限）
     * <p>
     * 该方法的调用时机为需授权资源被访问时，并且每次访问需授权资源都会执行该方法中的逻辑，这表明本例中并未启用AuthorizationCache，
     * 如果连续访问同一个URL（比如刷新），该方法不会被重复调用，Shiro有一个时间间隔（也就是cache时间，在ehcache-shiro.xml中配置），
     * 超过这个时间间隔再刷新页面，该方法会被执行
     * <p>
     * doGetAuthorizationInfo()是权限控制，
     * 当访问到页面的时候，使用了相应的注解或者shiro标签才会执行此方法否则不会执行，
     * 所以如果只是简单的身份认证没有权限的控制的话，那么这个方法可以不进行实现，直接返回null即可
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if (principals.getRealmNames().contains(getName())) {
            Object userName = super.getAvailablePrincipal(principals);
            if (userName != null) {
                UserBean user = userService.findOne(new StringCondition("username", userName));
                Set<String> roleSet = new HashSet<>();
                Set<String> permissionSet = new HashSet<>();
                info.setRoles(roleSet);
                info.setStringPermissions(permissionSet);
            }
        }
        //返回null将会导致用户访问任何被拦截的请求时都会自动跳转到unauthorizedUrl指定的地址
        return info;
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return principals.toString();
    }
}