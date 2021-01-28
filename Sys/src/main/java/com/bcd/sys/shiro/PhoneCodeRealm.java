package com.bcd.sys.shiro;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.redis.RedisUtil;
import com.bcd.base.config.shiro.ShiroMessageDefine;
import com.bcd.base.config.shiro.cache.RedisCache;
import com.bcd.base.config.shiro.realm.MyAuthorizingRealm;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.PermissionService;
import com.bcd.sys.service.UserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PhoneCodeRealm extends MyAuthorizingRealm {
    public PhoneCodeRealm(RedisConnectionFactory redisConnectionFactory) {
        setAuthenticationTokenClass(PhoneCodeToken.class);
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
                if(s.equals(getAuthorizationCacheName())){
                    return new RedisCache<>(redisTemplate,s,5000);
                }else{
                    return null;
                }
            }
        });
    }

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    @Qualifier("string_string_redisTemplate")
    private RedisTemplate<String,String> redisTemplate;



    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        PhoneCodeToken token = (PhoneCodeToken) authenticationToken;
        UserBean user = userService.findOne(
                Condition.and(
                        new StringCondition("phone", token.getPhone(), StringCondition.Handler.EQUAL)
                )
        );
        if (user != null) {
            if (user.getStatus() == null || user.getStatus() == 0) {
                throw ShiroMessageDefine.ERROR_SHIRO_DISABLED_ACCOUNT.toRuntimeException();
            }
            // 若存在，将此用户存放到登录认证info中
            String code=redisTemplate.opsForValue().get("phoneCode:"+token.getPhone());
            if(code==null){
                throw BaseRuntimeException.getException("验证码已过期,请重新获取");
            }
            SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user.getPhone(), code, getName());
            return simpleAuthenticationInfo;
        } else {
            throw ShiroMessageDefine.ERROR_SHIRO_UNKNOWN_ACCOUNT.toRuntimeException();
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if(principals.getRealmNames().contains(getName())) {
            Object phone = super.getAvailablePrincipal(principals);
            if (phone != null) {
                UserBean user = userService.findOne(new StringCondition("phone", phone));
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
