package com.bcd.sys.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;



import javax.persistence.*;

/**
 *  菜单表
 */
@Entity
@Table(name = "t_sys_menu")
public class MenuBean extends BaseBean<Long> {
    //field
    @ApiModelProperty(value = "父菜单id")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Length(max = 50,message = "[菜单名称]长度不能超过50")
    @ApiModelProperty(value = "菜单名称")
    private String name;

    @Length(max = 256,message = "[url地址]长度不能超过256")
    @ApiModelProperty(value = "url地址")
    private String url;

    @Length(max = 256,message = "[图标]长度不能超过256")
    @ApiModelProperty(value = "图标")
    private String icon;

    @NotNull(message = "排序不能为空")
    @ApiModelProperty(value = "排序")
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
