package com.bcd.sys.bean;


import com.alibaba.fastjson.annotation.JSONField;
import com.bcd.rdb.annotation.CheckReferredOnDelete;
import com.bcd.rdb.bean.BaseBean;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


/**
 * 组织机构基础信息
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Entity
@Table(name = "t_sys_org")
public class OrgBean extends BaseBean<Long> {
    private String name;  //组织名称
    private String address;  //地址
    private String phone;  //电话
    private String remark;  //备注
    private Long parentId;
    private Long orgItemId; //组织类型枚举项id

    @ManyToOne
    @JoinColumn(insertable = false,updatable = false,name = "orgItemId")
    private EnumItemBean enumItemDTO;

    @CheckReferredOnDelete
    @ManyToMany
    @JoinTable(
            name = "t_sys_org_role",
            joinColumns = @JoinColumn(name = "org_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id")
    )
    private Set<RoleBean> roleBeanSet = new HashSet<RoleBean>();

    @CheckReferredOnDelete
    @OneToMany(mappedBy = "orgId")
    private Set<UserBean> userBeanSet = new HashSet<>();

    @JSONField(serialize = false ,deserialize = false)
    @CheckReferredOnDelete
    @OneToMany(mappedBy = "parentId")
    private Set<OrgBean> orgBeanSet = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }


    public Set<RoleBean> getRoleBeanSet() {
        return roleBeanSet;
    }

    public void setRoleBeanSet(Set<RoleBean> roleBeanSet) {
        this.roleBeanSet = roleBeanSet;
    }

    public Set<UserBean> getUserBeanSet() {
        return userBeanSet;
    }

    public void setUserBeanSet(Set<UserBean> userBeanSet) {
        this.userBeanSet = userBeanSet;
    }

    public Long getOrgItemId() {
        return orgItemId;
    }

    public void setOrgItemId(Long orgItemId) {
        this.orgItemId = orgItemId;
    }

    public EnumItemBean getEnumItemDTO() {
        return enumItemDTO;
    }

    public void setEnumItemDTO(EnumItemBean enumItemDTO) {
        this.enumItemDTO = enumItemDTO;
    }

    public Set<OrgBean> getOrgBeanSet() {
        return orgBeanSet;
    }

    public void setOrgBeanSet(Set<OrgBean> orgBeanSet) {
        this.orgBeanSet = orgBeanSet;
    }
}
