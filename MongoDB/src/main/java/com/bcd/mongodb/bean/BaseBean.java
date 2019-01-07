package com.bcd.mongodb.bean;


import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/25.
 */
public class BaseBean<K extends Serializable> extends SuperBaseBean<K>{
    @ApiModelProperty(value = "创建时间(不需要赋值)")
    private Date createTime;

    @ApiModelProperty(value = "创建人id(不需要赋值)")
    private K createUserId;

    @Length(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)(不需要赋值)")
    private String createUserName;

    @ApiModelProperty(value = "更新时间(不需要赋值)")
    private Date updateTime;

    @ApiModelProperty(value = "更新人id(不需要赋值)")
    private K updateUserId;

    @Length(max = 50,message = "[更新人姓名]长度不能超过50")
    @ApiModelProperty(value = "更新人姓名(长度不能超过50)(不需要赋值)")
    private String updateUserName;

    @Length(max = 50,message = "[创建ip地址]长度不能超过50")
    @ApiModelProperty(value = "创建ip地址(长度不能超过50)(不需要赋值)")
    private String createIp;

    @Length(max = 50,message = "[更新ip地址]长度不能超过50")
    @ApiModelProperty(value = "更新ip地址(长度不能超过50)(不需要赋值)")
    private String updateIp;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public K getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(K createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public K getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(K updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getCreateIp() {
        return createIp;
    }

    public void setCreateIp(String createIp) {
        this.createIp = createIp;
    }

    public String getUpdateIp() {
        return updateIp;
    }

    public void setUpdateIp(String updateIp) {
        this.updateIp = updateIp;
    }
}
