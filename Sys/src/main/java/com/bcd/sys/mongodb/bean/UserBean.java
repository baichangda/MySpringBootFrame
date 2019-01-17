package com.bcd.sys.mongodb.bean;

import com.bcd.mongodb.bean.BaseBean;
import com.bcd.mongodb.code.CodeGenerator;
import com.bcd.mongodb.code.CollectionConfig;
import com.bcd.mongodb.code.Config;
import com.bcd.sys.UserDataAccess;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Document(collection = "user")
public class UserBean extends BaseBean<String> implements UserDataAccess<String>{

    @NotBlank(message = "[用户名]不能为空")
    @Size(max = 50,message = "[用户名]长度不能超过50")
    @ApiModelProperty(value = "用户名(不能为空,长度不能超过50)")
    //用户名
    private String username;

    @JsonIgnore
    @NotBlank(message = "[密码]不能为空")
    @Size(max = 100,message = "[密码]长度不能超过100")
    @ApiModelProperty(value = "密码(不能为空,长度不能超过100)")
    //密码
    private String password;

    @Size(max = 100,message = "[邮箱]长度不能超过100")
    @ApiModelProperty(value = "邮箱(长度不能超过100)")
    //邮箱
    private String email;

    @Size(max = 11,message = "[手机号]长度不能超过11")
    @ApiModelProperty(value = "手机号(长度不能超过11)")
    //手机号
    private String phone;

    @Size(max = 50,message = "[真实姓名]长度不能超过50")
    @ApiModelProperty(value = "真实姓名(长度不能超过50)")
    //真实名称
    private String realName;

    @Size(max = 5,message = "[性别]长度不能超过5")
    @ApiModelProperty(value = "性别(长度不能超过5)")
    //性别
    private String sex;

    @ApiModelProperty(value = "生日")
    //生日
    private Date birthday;

    @Size(max = 20,message = "[身份证号]长度不能超过20")
    @ApiModelProperty(value = "身份证号(长度不能超过20)")
    //身份证
    private String cardNumber;

    @NotNull(message = "[是否可用]不能为空")
    @ApiModelProperty(value = "是否可用(0:禁用,1:可用)(不能为空)")
    //状态
    private Integer status;

    //"Asia/Shanghai"
    @ApiModelProperty(hidden = true)
    @Transient
    private String timeZone;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public static void main(String [] args){
        Config configProperties=new Config(
                new CollectionConfig("User","用户", UserBean.class)
        );
        CodeGenerator.generate(configProperties);
    }
}
