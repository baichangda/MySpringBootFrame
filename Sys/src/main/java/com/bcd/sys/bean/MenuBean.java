package com.bcd.sys.rdb.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  菜单表
 */
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

    @NotNull(message = "排序不能为空")
    @ApiModelProperty(value = "排序(不能为空)")
    private Integer orderNum;


    //method
    public void setParentId(Long parentId){
        this.parentId=parentId;
    }

    public Long getParentId(){
        return this.parentId;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
    }

    public void setUrl(String url){
        this.url=url;
    }

    public String getUrl(){
        return this.url;
    }

    public void setIcon(String icon){
        this.icon=icon;
    }

    public String getIcon(){
        return this.icon;
    }

    public void setOrderNum(Integer orderNum){
        this.orderNum=orderNum;
    }

    public Integer getOrderNum(){
        return this.orderNum;
    }


}
