package com.bcd.sys.bean;

import com.bcd.rdb.bean.SuperBaseBean;
import com.bcd.sys.task.TaskConsumer;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "t_sys_task")
public class TaskBean extends SuperBaseBean<Long> {
    @ApiModelProperty(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)")
    private String filePaths;

    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)")
    private Integer type;

    //field
    @NotBlank(message = "任务名称不能为空")
    @Length(max = 50,message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称")
    public String name;


    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败;)")
    public Integer status;

    @Length(max = 255,message = "[备注]长度不能超过255")
    @ApiModelProperty(value = "备注(失败时记录失败原因)")
    public String remark;

    @ApiModelProperty(value = "任务开始时间")
    public Date startTime;

    @ApiModelProperty(value = "任务完成时间")
    public Date finishTime;

    @ApiModelProperty(value = "创建时间")
    public Date createTime;

    @ApiModelProperty(value = "创建人id")
    public Long createUserId;

    @Length(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名")
    public String createUserName;

    @Length(max = 50,message = "[创建ip]长度不能超过50")
    @ApiModelProperty(value = "创建ip")
    public String createIp;

    @Transient
    public TaskConsumer consumer;

    @Transient
    public TaskConsumer onStart;

    @Transient
    public TaskConsumer onSuccess;

    @Transient
    public TaskConsumer onFailed;

    public TaskBean(@NotBlank(message = "任务名称不能为空") @Length(max = 50, message = "[任务名称]长度不能超过50") String name,
                    @NotNull(message = "任务状态不能为空") Integer type) {
        this.name=name;
        this.type = type;
    }

    private TaskBean() {

    }

    public String getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(String filePaths) {
        this.filePaths = filePaths;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
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

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateIp() {
        return createIp;
    }

    public void setCreateIp(String createIp) {
        this.createIp = createIp;
    }

    public TaskConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(TaskConsumer consumer) {
        this.consumer = consumer;
    }

    public TaskConsumer getOnStart() {
        return onStart;
    }

    public void setOnStart(TaskConsumer onStart) {
        this.onStart = onStart;
    }

    public TaskConsumer getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(TaskConsumer onSuccess) {
        this.onSuccess = onSuccess;
    }

    public TaskConsumer getOnFailed() {
        return onFailed;
    }

    public void setOnFailed(TaskConsumer onFailed) {
        this.onFailed = onFailed;
    }
}
