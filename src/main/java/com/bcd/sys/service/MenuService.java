package com.bcd.sys.service;

import com.bcd.base.support_jdbc.service.BaseService;
import com.bcd.sys.bean.MenuBean;
import com.bcd.sys.define.CommonConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Administrator on 2017/4/11.
 */
@Service
public class MenuService extends BaseService<MenuBean> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 查询当前用户所属组织的admin拥有的权限的菜单树
     *
     * @return
     */
    public List<MenuBean> adminMenuTree() {
        String sql = "select * from t_sys_menu";
        List<MenuBean> menuBeanList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MenuBean.class));
        //3、组装成树并返回
        return listToTree(menuBeanList);
    }

    /**
     * 查询用户拥有的权限的菜单树
     *
     * @return
     */
    public List<MenuBean> userMenuTree(Long userId) {
        List<MenuBean> menuBeanList;
        if (CommonConst.ADMIN_ID == userId) {
            String sql = "select * from t_sys_menu";
            menuBeanList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MenuBean.class));
        } else {
            String sql = "select tc.* from (SELECT * FROM t_sys_user_role WHERE user_id = ?) a " +
                    "inner join t_sys_role_menu b ON a.role_id = b.role_id " +
                    "inner join t_sys_menu c ON b.menu_id = c.id ";
            menuBeanList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MenuBean.class), userId);
        }
        return listToTree(menuBeanList);
    }

    private List<MenuBean> listToTree(List<MenuBean> menuBeanList) {
        if (menuBeanList == null || menuBeanList.isEmpty()) {
            return Collections.emptyList();
        }
        //1、组装菜单树,从顶级菜单开始
        //1.1、转化数据集
        Map<Long, List<MenuBean>> parentIdToChildrenMap = menuBeanList.stream().collect(Collectors.toMap(e -> e.parentId, e -> {
            List<MenuBean> childrenList = new ArrayList<>();
            childrenList.add(e);
            return childrenList;
        }, (e1, e2) -> {
            e1.addAll(e2);
            return e1;
        }));
        //1.2、取出根,依次遍历
        List<MenuBean> rootList = parentIdToChildrenMap.get(null);
        if (rootList == null || rootList.isEmpty()) {
            return Collections.emptyList();
        }
        //1.3、排序
        rootList.sort(Comparator.comparing(e -> e.orderNum));
        //1.4、循环填充
        List<MenuBean> tempList = new ArrayList<>(rootList);
        for (int i = 0; i <= tempList.size() - 1; i++) {
            MenuBean cur = tempList.get(i);
            List<MenuBean> curChildren = parentIdToChildrenMap.get(cur.id);
            if (curChildren != null) {
                curChildren.sort(Comparator.comparing(e -> e.orderNum));
                cur.children = curChildren;
                tempList.addAll(curChildren);
            }
        }
        //2、返回根集合
        return rootList;
    }
}
