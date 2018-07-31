package com.bcd.sys.bean;


import com.bcd.rdb.bean.BaseBean;

import javax.persistence.*;

/**
 * 角色
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Entity
@Table(name = "t_sys_role")
public class RoleBean extends BaseBean<Long> {
    private String name;  //角色名称
    private String code; //角色编码(必须以Role_开头)
    private String remark;  //备注

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
