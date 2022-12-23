package com.bcd.sys.bean;

import com.bcd.base.support_jpa.bean.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单表
 */
@Entity
@Table(name = "t_sys_menu")
public class MenuBean extends BaseBean<Long> {
    //field
    @Schema(description = "父菜单id")
    public Long parentId;

    @NotBlank(message = "[菜单名称]不能为空")
    @Size(max = 50, message = "[菜单名称]长度不能超过50")
    @Schema(description = "菜单名称", required = true, maxLength = 50)
    public String name;

    @Size(max = 256, message = "[url地址]长度不能超过256")
    @Schema(description = "url地址", maxLength = 256)
    public String url;

    @Size(max = 256, message = "[图标]长度不能超过256")
    @Schema(description = "图标", maxLength = 256)
    public String icon;

    @NotNull(message = "[排序]不能为空")
    @Schema(description = "排序", required = true)
    public Integer orderNum;

    @Transient
    public List<MenuBean> children = new ArrayList<>();
}
