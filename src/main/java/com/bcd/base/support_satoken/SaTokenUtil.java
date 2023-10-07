package com.bcd.base.support_satoken;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class SaTokenUtil {

    static UserServiceInterface userService;

    static volatile LoadingCache<String, Object> loginId_user;

    final static Duration expire = Duration.ofSeconds(3);

    @Autowired
    public void setCacheService(UserServiceInterface userService) {
        SaTokenUtil.userService = userService;
    }

    public static Object getLoginUser_cache() {
        if (loginId_user == null) {
            synchronized (SaTokenUtil.class) {
                if (loginId_user == null) {
                    loginId_user = Caffeine.newBuilder().expireAfterWrite(expire).build(k ->  userService.getUserByLoginId(k));
                }
            }
        }
        final String loginIdAsString = StpUtil.getLoginIdAsString();
        return Optional.ofNullable(loginIdAsString).map(e -> loginId_user.get(e)).orElse(null);
    }

    public static Object getLoginUser() {
        try {
            final String loginIdAsString = StpUtil.getLoginIdAsString();
            return Optional.ofNullable(loginIdAsString).map(e -> userService.getUserByLoginId(e)).orElse(null);
        } catch (SaTokenException ex) {
            return null;
        }
    }
}
