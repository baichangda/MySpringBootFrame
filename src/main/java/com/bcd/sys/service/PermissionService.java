package com.bcd.sys.service;

import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.rdb.service.BaseService;
import com.bcd.sys.bean.PermissionBean;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PermissionService extends BaseService<PermissionBean, Long> implements SpringInitializable {
    @Override
    public void init(ContextRefreshedEvent event) {
        initNotePermission(event);
    }

    @Transactional
    public void initNotePermission(ContextRefreshedEvent event) {
        //1、扫描所有已经使用过的 NotePermission
        Map<String, Object> beanMap = event.getApplicationContext().getBeansWithAnnotation(Controller.class);
        Set<NotePermission> permissionSet = new LinkedHashSet<>();
        beanMap.values().forEach(e1 -> {
            Class controllerClass=ClassUtils.getUserClass(e1);
            List<Method> methodList = MethodUtils.getMethodsListWithAnnotation(controllerClass, RequiresNotePermissions.class);
            methodList.forEach(e2 -> {
                RequiresNotePermissions requiresNotePermissions = e2.getAnnotation(RequiresNotePermissions.class);
                permissionSet.addAll(Arrays.stream(requiresNotePermissions.value()).collect(Collectors.toSet()));
            });
        });

        //2、清空权限表
        deleteAllInBatch();

        //3、转换成实体类并保存
        List<PermissionBean> permissionBeanList = permissionSet.stream().map(e -> {
            PermissionBean permissionBean = new PermissionBean();
            permissionBean.setCode(e.getCode());
            permissionBean.setName(e.getNote());
            return permissionBean;
        }).collect(Collectors.toList());
        ((PermissionService) AopContext.currentProxy()).saveBatch(permissionBeanList);
    }

}
