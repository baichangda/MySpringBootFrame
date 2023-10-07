package com.bcd.sys.service;

import com.bcd.sys.bean.UserBean;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CacheService {
    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    PermissionService permissionService;

    final Duration expire = Duration.ofSeconds(3);

    private volatile LoadingCache<String, UserBean> username_user;
    private volatile LoadingCache<String, List<String>> username_roleList;
    private volatile LoadingCache<String, List<String>> username_permissionList;

    public UserBean getUser(String username) {
        if (username_user == null) {
            synchronized (this) {
                if (username_user == null) {
                    username_user = Caffeine.newBuilder().expireAfterWrite(expire).build(k -> userService.getUser(k));
                }
            }
        }
        return username_user.get(username);
    }

    public List<String> getRoleList(String username, String loginType) {
        if (username_roleList == null) {
            synchronized (this) {
                if (username_roleList == null) {
                    username_roleList = Caffeine.newBuilder().expireAfterWrite(expire).build(k -> {
                        final UserBean user = userService.getUser(k);
                        if (user == null) {
                            return Collections.emptyList();
                        } else {
                            return roleService.findRolesByUserId(user.id).stream().map(e->e.code).collect(Collectors.toList());
                        }
                    });
                }
            }
        }
        return username_roleList.get(username);
    }

    public List<String> getPermissionList(String username, String loginType) {
        if (username_permissionList == null) {
            synchronized (this) {
                if (username_permissionList == null) {
                    username_permissionList = Caffeine.newBuilder().expireAfterWrite(expire).build(k -> {
                        final UserBean user = userService.getUser(k);
                        if (user == null) {
                            return Collections.emptyList();
                        } else {
                            return permissionService.findPermissionsByUserId(user.id).stream().map(e->e.code).collect(Collectors.toList());
                        }
                    });
                }
            }
        }
        return username_permissionList.get(username);
    }
}
