package com.sys.bean;


import com.alibaba.fastjson.annotation.JSONField;
import com.base.annotation.ReferredCollection;
import com.bcd.rdb.bean.BaseBean;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 菜单
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Entity
@Table(name = "t_sys_menu")
public class MenuBean extends BaseBean {
    private String name;  //菜单名称
    private String url;  //url地址
    private String icon;  //图标
    private Integer orderNum;  //排序
    private Long parentId;
    private Long menuItemId;

    //角色关联菜单
    @ReferredCollection
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_sys_role_menu",
            joinColumns = @JoinColumn(name = "menu_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id")
    )
    private Set<RoleBean> roleBeanSet = new HashSet<RoleBean>();

    @ManyToOne
    @JoinColumn(insertable = false,updatable = false,name = "menuItemId")
    private EnumItemBean enumItemDTO;

    @ReferredCollection
    @JSONField(serialize=false,deserialize = false)
    @OneToMany(mappedBy = "parentId")
    @OrderBy("orderNum ASC")
    private List<MenuBean> childrenList = new ArrayList<>();

    @Transient
    private List<MenuBean> userChildrenList=new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<MenuBean> getChildrenList() {
        return childrenList;
    }

    public void setChildrenList(List<MenuBean> childrenList) {
        this.childrenList = childrenList;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public EnumItemBean getEnumItemDTO() {
        return enumItemDTO;
    }

    public void setEnumItemDTO(EnumItemBean enumItemDTO) {
        this.enumItemDTO = enumItemDTO;
    }

    public Set<RoleBean> getRoleBeanSet() {
        return roleBeanSet;
    }

    public void setRoleBeanSet(Set<RoleBean> roleBeanSet) {
        this.roleBeanSet = roleBeanSet;
    }

    public List<MenuBean> getUserChildrenList() {
        return userChildrenList;
    }

    public void setUserChildrenList(List<MenuBean> userChildrenList) {
        this.userChildrenList = userChildrenList;
    }
}
