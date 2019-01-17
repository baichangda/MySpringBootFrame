package com.bcd.sys.rdb.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  角色与权限关系表
 */
@Entity
@Table(name = "t_sys_permission")
public class PermissionBean extends BaseBean<Long> {
    //field
    @NotBlank(message = "[角色名称]不能为空")
    @Size(max = 20,message = "[角色名称]长度不能超过20")
    @ApiModelProperty(value = "角色名称(不能为空,长度不能超过20)")
    private String name;

    @NotBlank(message = "[编码]不能为空")
    @Size(max = 100,message = "[编码]长度不能超过100")
    @ApiModelProperty(value = "编码(不能为空,长度不能超过100)")
    private String code;

    @Size(max = 256,message = "[备注]长度不能超过256")
    @ApiModelProperty(value = "备注(长度不能超过256)")
    private String remark;

    @ApiModelProperty(value = "关联角色id")
    private Long roleId;


    //method
    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
    }

    public void setCode(String code){
        this.code=code;
    }

    public String getCode(){
        return this.code;
    }

    public void setRemark(String remark){
        this.remark=remark;
    }

    public String getRemark(){
        return this.remark;
    }

    public void setRoleId(Long roleId){
        this.roleId=roleId;
    }

    public Long getRoleId(){
        return this.roleId;
    }


}
