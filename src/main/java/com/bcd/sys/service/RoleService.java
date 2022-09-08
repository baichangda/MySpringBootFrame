package com.bcd.sys.service;

import com.bcd.base.support_jpa.service.BaseService;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.define.CommonConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2017/4/11.
 */
@Service
public class RoleService extends BaseService<RoleBean, Long> {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<RoleBean> findRolesByUserId(Long userId){
        if (CommonConst.ADMIN_ID == userId) {
            return findAll();
        }else {
            String sql = "select b.* from t_sys_user_role a inner join t_sys_role b on a.role_code=b.code where a.user_id=?";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RoleBean.class), userId);
        }
    }
}
