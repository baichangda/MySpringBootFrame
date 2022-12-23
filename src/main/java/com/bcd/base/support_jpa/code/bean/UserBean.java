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
 *  用户
 */
@Entity
@Table(name = "t_sys_user")
public class UserBean extends BaseBean<Long> {
    //field
    @Schema(description = "生日")
    public Date birthday;

    @Schema(description = "身份证号", maxLength = 20)
    @Size(max = 20, message = "[身份证号]长度不能超过20")
    public String cardNumber;

    @Schema(description = "邮箱", maxLength = 100)
    @Size(max = 100, message = "[邮箱]长度不能超过100")
    public String email;

    @Schema(description = "密码", required = true, maxLength = 100)
    @NotBlank(message = "[密码]不能为空")
    @Size(max = 100, message = "[密码]长度不能超过100")
    public String password;

    @Schema(description = "手机号", maxLength = 11)
    @Size(max = 11, message = "[手机号]长度不能超过11")
    public String phone;

    @Schema(description = "真实姓名", maxLength = 50)
    @Size(max = 50, message = "[真实姓名]长度不能超过50")
    public String realName;

    @Schema(description = "性别", maxLength = 5)
    @Size(max = 5, message = "[性别]长度不能超过5")
    public String sex;

    @Schema(description = "是否可用(0:禁用,1:可用)", required = true)
    @NotNull(message = "[是否可用]不能为空")
    public Integer status;

    @Schema(description = "用户名", required = true, maxLength = 50)
    @NotBlank(message = "[用户名]不能为空")
    @Size(max = 50, message = "[用户名]长度不能超过50")
    public String username;

}
