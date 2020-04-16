package com.bcd.sys.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;



import javax.persistence.*;

/**
 *  角色与权限关系表
 */
@Entity
@Table(name = "t_sys_permission")
public class PermissionBean extends BaseBean<Long> {
    //field
    @NotBlank(message = "[编码]不能为空")
    @Size(max = 50,message = "[编码]长度不能超过50")
    @ApiModelProperty(value = "编码(不能为空,长度不能超过50)")
    private String code;

    @NotBlank(message = "[角色名称]不能为空")
    @Size(max = 20,message = "[角色名称]长度不能超过20")
    @ApiModelProperty(value = "角色名称(不能为空,长度不能超过20)")
    private String name;

    @Size(max = 256,message = "[备注]长度不能超过256")
    @ApiModelProperty(value = "备注(长度不能超过256)")
    private String remark;

    @ApiModelProperty(value = "关联角色id")
    private Long roleId;


    //method
    public void setCode(String code){
        this.code=code;
    }

    public String getCode(){
        return this.code;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
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
