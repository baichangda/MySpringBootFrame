package com.bcd.sys.rdb.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  组织机构基础信息表
 */
@Entity
@Table(name = "t_sys_org")
public class OrgBean extends BaseBean<Long> {
    //field
    @ApiModelProperty(value = "父组织id")
    private Long parentId;

    @NotBlank(message = "[组织名称]不能为空")
    @Size(max = 50,message = "[组织名称]长度不能超过50")
    @ApiModelProperty(value = "组织名称(不能为空,长度不能超过50)")
    private String name;

    @Size(max = 256,message = "[地址]长度不能超过256")
    @ApiModelProperty(value = "地址(长度不能超过256)")
    private String address;

    @Size(max = 11,message = "[电话]长度不能超过11")
    @ApiModelProperty(value = "电话(长度不能超过11)")
    private String phone;

    @Size(max = 256,message = "[备注]长度不能超过256")
    @ApiModelProperty(value = "备注(长度不能超过256)")
    private String remark;


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

    public void setAddress(String address){
        this.address=address;
    }

    public String getAddress(){
        return this.address;
    }

    public void setPhone(String phone){
        this.phone=phone;
    }

    public String getPhone(){
        return this.phone;
    }

    public void setRemark(String remark){
        this.remark=remark;
    }

    public String getRemark(){
        return this.remark;
    }


}
