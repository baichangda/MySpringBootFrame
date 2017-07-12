package com.base.bean;

import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/11.
 */
@MappedSuperclass
public class BaseBean extends SuperBaseBean {

    public Date createTime;

    public Date updateTime;

    public Long createUserId;

    public Long updateUserId;

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

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

}
