package com.bcd.sys.bean;

import com.bcd.base.support_jpa.bean.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 角色表
 */
@Entity
@Table(name = "t_sys_role")
public class RoleBean extends BaseBean<Long> {
    //field
    @NotBlank(message = "[角色名称]不能为空")
    @Size(max = 20, message = "[角色名称]长度不能超过20")
    @Schema(description = "角色名称", required = true, maxLength = 20)
    public String name;

    @NotBlank(message = "[编码]不能为空")
    @Size(max = 50, message = "[编码]长度不能超过50")
    @Schema(description = "编码", required = true, maxLength = 50)
    public String code;

    @Size(max = 256, message = "[备注]长度不能超过256")
    @Schema(description = "备注", maxLength = 256)
    public String remark;


}
