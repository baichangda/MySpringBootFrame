package com.bcd.sys.bean;

import com.bcd.rdb.bean.SuperBaseBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.Lazy;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 *  系统任务处理表
 */
@Entity
@Table(name = "t_sys_task")
public class TaskBean extends SuperBaseBean<Long> {


    //field
    @NotBlank(message = "[任务名称]不能为空")
    @Length(max = 50,message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称(不能为空,长度不能超过50)")
    private String name;

    @NotNull(message = "[任务状态]不能为空")
    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)(不能为空)")
    private Integer status;

    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)(不能为空)")
    private Integer type;

    @Length(max = 255,message = "[备注]长度不能超过255")
    @ApiModelProperty(value = "备注(失败时记录失败原因)(长度不能超过255)")
    private String remark;

    @Length(max = 65535,message = "[失败堆栈信息]长度不能超过65535")
    @ApiModelProperty(hidden = true,readOnly = true,value = "失败堆栈信息(失败时后台异常堆栈信息)(长度不能超过65535)")
    @Column(columnDefinition="TEXT")
    @Lazy
    @JsonIgnore
    private String stackMessage;

    @ApiModelProperty(value = "任务开始时间")
    private Date startTime;

    @ApiModelProperty(value = "任务完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Length(max = 100,message = "[文件路径]长度不能超过100")
    @ApiModelProperty(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)(长度不能超过100)")
    private String filePaths;

    @ApiModelProperty(value = "创建人id")
    private Long createUserId;

    @Length(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)")
    private String createUserName;

    @Length(max = 50,message = "[创建ip]长度不能超过50")
    @ApiModelProperty(value = "创建ip(长度不能超过50)")
    private String createIp;

    public TaskBean(String name) {
        this.name=name;
    }

    private TaskBean() {
    }

    //method
    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
    }

    public void setStatus(Integer status){
        this.status=status;
    }

    public Integer getStatus(){
        return this.status;
    }

    public void setType(Integer type){
        this.type=type;
    }

    public Integer getType(){
        return this.type;
    }

    public void setRemark(String remark){
        this.remark=remark;
    }

    public String getRemark(){
        return this.remark;
    }

    public void setStartTime(Date startTime){
        this.startTime=startTime;
    }

    public Date getStartTime(){
        return this.startTime;
    }

    public void setFinishTime(Date finishTime){
        this.finishTime=finishTime;
    }

    public Date getFinishTime(){
        return this.finishTime;
    }

    public void setCreateTime(Date createTime){
        this.createTime=createTime;
    }

    public Date getCreateTime(){
        return this.createTime;
    }

    public void setFilePaths(String filePaths){
        this.filePaths=filePaths;
    }

    public String getFilePaths(){
        return this.filePaths;
    }

    public void setCreateUserId(Long createUserId){
        this.createUserId=createUserId;
    }

    public Long getCreateUserId(){
        return this.createUserId;
    }

    public void setCreateUserName(String createUserName){
        this.createUserName=createUserName;
    }

    public String getCreateUserName(){
        return this.createUserName;
    }

    public void setCreateIp(String createIp){
        this.createIp=createIp;
    }

    public String getCreateIp(){
        return this.createIp;
    }

    public String getStackMessage() {
        return stackMessage;
    }

    public void setStackMessage(String stackMessage) {
        this.stackMessage = stackMessage;
    }
}
