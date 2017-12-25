package com.bcd.sys.bean;


import com.bcd.rdb.annotation.CheckRepeatReferOnSave;
import com.bcd.rdb.annotation.CheckReferredOnDelete;
import com.bcd.rdb.bean.BaseBean;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    @CheckReferredOnDelete
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_sys_user_role",
            joinColumns=@JoinColumn(name = "role_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id")
    )
    private Set<UserBean> userBeanSet = new HashSet<>();

    //角色关联菜单
    @CheckRepeatReferOnSave
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_sys_role_menu",
            joinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id",referencedColumnName = "id")
    )
    private Set<MenuBean> menuBeanSet = new HashSet<MenuBean>();

    //角色关联机构
    @CheckRepeatReferOnSave
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_sys_org_role",
            joinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "org_id",referencedColumnName = "id")
    )
    private Set<OrgBean> orgBeanSet = new HashSet<OrgBean>();

    @CheckReferredOnDelete
    @OneToMany(mappedBy = "roleId")
    private Set<PermissionBean> permissionBeanSet = new HashSet<>();

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

    public Set<MenuBean> getMenuBeanSet() {
        return menuBeanSet;
    }

    public void setMenuBeanSet(Set<MenuBean> menuBeanSet) {
        this.menuBeanSet = menuBeanSet;
    }

    public Set<OrgBean> getOrgBeanSet() {
        return orgBeanSet;
    }

    public void setOrgBeanSet(Set<OrgBean> orgBeanSet) {
        this.orgBeanSet = orgBeanSet;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<UserBean> getUserBeanSet() {
        return userBeanSet;
    }

    public void setUserBeanSet(Set<UserBean> userBeanSet) {
        this.userBeanSet = userBeanSet;
    }

    public Set<PermissionBean> getPermissionBeanSet() {
        return permissionBeanSet;
    }

    public void setPermissionBeanSet(Set<PermissionBean> permissionBeanSet) {
        this.permissionBeanSet = permissionBeanSet;
    }
}
