package com.bcd.sys.service;

import com.bcd.base.support_spring_init.SpringInitializable;
import com.bcd.base.support_shiro.anno.RequiresNotePermissions;
import com.bcd.base.support_shiro.data.NotePermission;
import com.bcd.base.support_jpa.service.BaseService;
import com.bcd.sys.bean.PermissionBean;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

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
            Class controllerClass = ClassUtils.getUserClass(e1);
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
        ((PermissionService) AopContext.currentProxy()).saveAll(permissionBeanList);
    }

    public List<PermissionBean> findPermissionsByUserId(Long userId) {
        String sql = """
                select d.* from t_sys_user_role a
                inner join t_sys_role_menu b on b.role_id=a.role_id
                inner join t_sys_menu_permission c on b.menu_id=c.menu_id
                inner join t_sys_permission d on c.permission_code=d.code
                where a.user_id= ?
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PermissionBean.class), userId);
    }

}
