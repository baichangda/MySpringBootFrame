package com.bcd.sys.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;


import javax.persistence.*;

/**
 *  菜单表
 */
@Data
@Entity
@Table(name = "t_sys_menu")
public class MenuBean extends BaseBean<Long> {
    //field
    @ApiModelProperty(value = "父菜单id")
    private Long parentId;

    @NotBlank(message = "[菜单名称]不能为空")
    @Size(max = 50,message = "[菜单名称]长度不能超过50")
    @ApiModelProperty(value = "菜单名称(不能为空,长度不能超过50)")
    private String name;

    @Size(max = 256,message = "[url地址]长度不能超过256")
    @ApiModelProperty(value = "url地址(长度不能超过256)")
    private String url;

    @Size(max = 256,message = "[图标]长度不能超过256")
    @ApiModelProperty(value = "图标(长度不能超过256)")
    private String icon;

    @NotNull(message = "[排序]不能为空")
    @ApiModelProperty(value = "排序(不能为空)")
    private Integer orderNum;

    @Transient
    private List<MenuBean> children=new ArrayList<>();
}
