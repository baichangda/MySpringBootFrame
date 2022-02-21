package com.bcd.sys.service;

import com.bcd.sys.bean.UserBean;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheService {
    @Autowired
    UserService userService;

    private volatile LoadingCache<String, UserBean> cache;

    public UserBean getUser(String username) {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(k -> userService.getUser(k));
                }
            }
        }
        return cache.get(username);
    }
}
