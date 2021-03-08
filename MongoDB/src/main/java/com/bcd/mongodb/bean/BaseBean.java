package com.bcd.mongodb.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/25.
 */
@Accessors(chain = true)
@Getter
@Setter
public class BaseBean<K extends Serializable> extends SuperBaseBean<K> {
    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    protected Date createTime;

    @Schema(description = "创建人id", accessMode = Schema.AccessMode.READ_ONLY)
    protected K createUserId;

    @Length(max = 50, message = "[创建人姓名]长度不能超过50")
    @Schema(description = "创建人姓名", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    protected String createUserName;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    protected Date updateTime;

    @Schema(description = "更新人id", accessMode = Schema.AccessMode.READ_ONLY)
    protected K updateUserId;

    @Length(max = 50, message = "[更新人姓名]长度不能超过50")
    @Schema(description = "更新人姓名", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    protected String updateUserName;

    @Length(max = 50, message = "[创建ip地址]长度不能超过50")
    @Schema(description = "创建ip地址", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    protected String createIp;

    @Length(max = 50, message = "[更新ip地址]长度不能超过50")
    @Schema(description = "更新ip地址", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    protected String updateIp;

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
