package com.bcd.sys.bean;

import com.bcd.rdb.bean.BaseBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 角色表
 */
@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "t_sys_role")
public class RoleBean extends BaseBean<Long> {
    //field
    @NotBlank(message = "[角色名称]不能为空")
    @Size(max = 20, message = "[角色名称]长度不能超过20")
    @ApiModelProperty(value = "角色名称(不能为空,长度不能超过20)")
    private String name;

    @NotBlank(message = "[编码]不能为空")
    @Size(max = 50, message = "[编码]长度不能超过50")
    @ApiModelProperty(value = "编码(不能为空,长度不能超过50)")
    private String code;

    @Size(max = 256, message = "[备注]长度不能超过256")
    @ApiModelProperty(value = "备注(长度不能超过256)")
    private String remark;


}
