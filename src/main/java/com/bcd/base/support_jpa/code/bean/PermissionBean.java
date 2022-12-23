package com.bcd.base.support_jpa.code.bean;

import com.bcd.base.support_jpa.bean.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import javax.persistence.*;

/**
 *  权限
 */
@Entity
@Table(name = "t_sys_permission")
public class PermissionBean extends BaseBean<Long> {
    //field
    @Schema(description = "编码", required = true, maxLength = 50)
    @NotBlank(message = "[编码]不能为空")
    @Size(max = 50, message = "[编码]长度不能超过50")
    public String code;

    @Schema(description = "角色名称", required = true, maxLength = 20)
    @NotBlank(message = "[角色名称]不能为空")
    @Size(max = 20, message = "[角色名称]长度不能超过20")
    public String name;

    @Schema(description = "备注", maxLength = 256)
    @Size(max = 256, message = "[备注]长度不能超过256")
    public String remark;

}
