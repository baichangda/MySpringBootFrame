package com.bcd.base.support_satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.bcd.sys.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyStpInterface implements StpInterface {


    @Autowired
    CacheService cacheService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return cacheService.getPermissionList(loginId.toString(), loginType);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return cacheService.getRoleList(loginId.toString(), loginType);
    }
}
