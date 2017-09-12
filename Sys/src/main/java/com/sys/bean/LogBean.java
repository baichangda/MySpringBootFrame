package com.sys.bean;

import com.bcd.rdb.bean.SuperBaseBean;

import javax.persistence.*;
import java.util.Date;

/**
 * 日志
 *
 * @author Aaric
 * @since 2017-05-03
 */
@Entity
@Table(name = "t_log")
public class LogBean extends SuperBaseBean {

    private Integer operType; //操作

    private Long resourceId;//资源主键

    private String resourceClass;//资源class全名

    private Date createTime; //记录时间
    private Long createUserId; //操作人ID

    public LogBean(Integer operType) {
        this.operType = operType;
    }

    public LogBean() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOperType() {
        return operType;
    }

    public void setOperType(Integer operType) {
        this.operType = operType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }
}
