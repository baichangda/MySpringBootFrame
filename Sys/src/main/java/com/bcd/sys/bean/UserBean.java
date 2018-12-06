package com.bcd.sys.bean;

import com.bcd.rdb.bean.BaseBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;



import javax.persistence.*;

/**
 *  用户基础信息表
 */
@Entity
@Table(name = "t_sys_user")
public class UserBean extends BaseBean<Long> {
    //field
    @ApiModelProperty(value = "关联机构id")
    private Long orgId;

    @NotBlank(message = "[用户名]不能为空")
    @Length(max = 50,message = "[用户名]长度不能超过50")
    @ApiModelProperty(value = "用户名(不能为空,长度不能超过50)")
    private String username;

    @JsonIgnore
    @NotBlank(message = "[密码]不能为空")
    @Length(max = 100,message = "[密码]长度不能超过100")
    @ApiModelProperty(value = "密码(不能为空,长度不能超过100)")
    private String password;

    @Length(max = 100,message = "[邮箱]长度不能超过100")
    @ApiModelProperty(value = "邮箱(长度不能超过100)")
    private String email;

    @Length(max = 11,message = "[手机号]长度不能超过11")
    @ApiModelProperty(value = "手机号(长度不能超过11)")
    private String phone;

    @Length(max = 50,message = "[真实姓名]长度不能超过50")
    @ApiModelProperty(value = "真实姓名(长度不能超过50)")
    private String realName;

    @Length(max = 5,message = "[性别]长度不能超过5")
    @ApiModelProperty(value = "性别(长度不能超过5)")
    private String sex;

    @ApiModelProperty(value = "生日")
    private Date birthday;

    @Length(max = 20,message = "[身份证号]长度不能超过20")
    @ApiModelProperty(value = "身份证号(长度不能超过20)")
    private String cardNumber;

    @NotNull(message = "[是否可用]不能为空")
    @ApiModelProperty(value = "是否可用(0:禁用,1:可用)(不能为空)")
    private Integer status;

    //"Asia/Shanghai"
    @ApiModelProperty(hidden = true)
    @Transient
    private String timeZone;

    //method
    public void setOrgId(Long orgId){
        this.orgId=orgId;
    }

    public Long getOrgId(){
        return this.orgId;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public String getUsername(){
        return this.username;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public String getPassword(){
        return this.password;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setPhone(String phone){
        this.phone=phone;
    }

    public String getPhone(){
        return this.phone;
    }

    public void setRealName(String realName){
        this.realName=realName;
    }

    public String getRealName(){
        return this.realName;
    }

    public void setSex(String sex){
        this.sex=sex;
    }

    public String getSex(){
        return this.sex;
    }

    public void setBirthday(Date birthday){
        this.birthday=birthday;
    }

    public Date getBirthday(){
        return this.birthday;
    }

    public void setCardNumber(String cardNumber){
        this.cardNumber=cardNumber;
    }

    public String getCardNumber(){
        return this.cardNumber;
    }

    public void setStatus(Integer status){
        this.status=status;
    }

    public Integer getStatus(){
        return this.status;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
