package com.bcd.base.support_satoken;

import cn.dev33.satoken.stp.StpUtil;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.CacheService;
import com.bcd.sys.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaTokenUtil {

    static CacheService cacheService;

    static UserService userService;

    @Autowired
    public void setCacheService(CacheService cacheService) {
        SaTokenUtil.cacheService = cacheService;
    }

    public static UserBean getLoginUser_cache(){
        return cacheService.getUser(StpUtil.getLoginIdAsString());
    }

    public static UserBean getLoginUser(){
        return userService.getUser(StpUtil.getLoginIdAsString());
    }

}
