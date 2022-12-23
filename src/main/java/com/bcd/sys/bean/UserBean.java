package com.bcd.sys.bean;

import com.bcd.base.support_jpa.bean.BaseBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 用户基础信息表
 */
@Entity
@Table(name = "t_sys_user")
public class UserBean extends BaseBean<Long> {
    private final static long serialVersionUID = 1L;

    //field
    @NotBlank(message = "[用户名]不能为空")
    @Size(max = 50, message = "[用户名]长度不能超过50")
    @Schema(description = "用户名", maxLength = 50, required = true)
    public String username;

    @JsonIgnore
    @Schema(description = "密码", maxLength = 100)
    public String password;

    @Size(max = 100, message = "[邮箱]长度不能超过100")
    @Schema(description = "邮箱", maxLength = 100)
    public String email;

    @Size(max = 11, message = "[手机号]长度不能超过11")
    @Schema(description = "手机号", maxLength = 11)
    public String phone;

    @Size(max = 50, message = "[真实姓名]长度不能超过50")
    @Schema(description = "真实姓名", maxLength = 50)
    public String realName;

    @Size(max = 5, message = "[性别]长度不能超过5")
    @Schema(description = "性别", maxLength = 5)
    public String sex;

    @Schema(description = "生日")
    public Date birthday;

    @Size(max = 20, message = "[身份证号]长度不能超过20")
    @Schema(description = "身份证号", maxLength = 20)
    public String cardNumber;

    @NotNull(message = "[是否可用]不能为空")
    @Schema(description = "是否可用(0:禁用,1:可用)", required = true)
    public Integer status;
}
