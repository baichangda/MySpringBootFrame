package com.bcd.base.support_satoken;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.CacheService;
import com.bcd.sys.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SaTokenUtil {

    static CacheService cacheService;

    static UserService userService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        SaTokenUtil.cacheService = cacheService;
    }

    public static UserBean getLoginUser_cache() {
        try {
            final String loginIdAsString = StpUtil.getLoginIdAsString();
            return Optional.ofNullable(loginIdAsString).map(e -> cacheService.getUser(e)).orElse(null);
        } catch (SaTokenException ex) {
            return null;
        }
    }

    public static UserBean getLoginUser() {
        try {
            final String loginIdAsString = StpUtil.getLoginIdAsString();
            return Optional.ofNullable(loginIdAsString).map(e -> userService.getUser(e)).orElse(null);
        } catch (SaTokenException ex) {
            return null;
        }
    }
}
