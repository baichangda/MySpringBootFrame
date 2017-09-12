package com.sys.bean;

import com.bcd.rdb.bean.BaseBean;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/7/12.
 */
@Entity
@Table(name = "t_sys_permission")
public class PermissionBean extends BaseBean {
    private String name;
    private String code;
    private String remark;
    private Long roleId;
    @ManyToOne
    @JoinColumn(name = "roleId",insertable = false,updatable = false)
    private RoleBean role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public RoleBean getRole() {
        return role;
    }

    public void setRole(RoleBean role) {
        this.role = role;
    }
}
