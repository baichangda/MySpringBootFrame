package com.bcd.sys.bean;

import com.bcd.rdb.bean.SuperBaseBean;
import com.bcd.sys.task.TaskConsumer;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;



import javax.persistence.*;

/**
 *  系统任务处理表
 */
@Entity
@Table(name = "t_sys_task")
public class TaskBean extends SuperBaseBean<Long> {
    //field
    @NotBlank(message = "任务名称不能为空")
    @Length(max = 50,message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称(不能为空,长度不能超过50)")
    private String name;

    @NotNull(message = "任务状态不能为空")
    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败;)(不能为空)")
    private Integer status;

    @NotNull(message = "任务类型不能为空")
    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)(不能为空)")
    private Integer type;

    @Length(max = 255,message = "[备注]长度不能超过255")
    @ApiModelProperty(value = "备注(失败时记录失败原因)(长度不能超过255)")
    private String remark;

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


    @Transient
    @ApiModelProperty(hidden = true)
    public TaskConsumer consumer;

    @Transient
    @ApiModelProperty(hidden = true)
    public TaskConsumer onStart;

    @Transient
    @ApiModelProperty(hidden = true)
    public TaskConsumer onSuccess;

    @Transient
    @ApiModelProperty(hidden = true)
    public TaskConsumer onFailed;

    public TaskBean(@NotBlank(message = "任务名称不能为空") @Length(max = 50, message = "[任务名称]长度不能超过50") String name,
                    @NotNull(message = "任务状态不能为空") Integer type) {
        this.name=name;
        this.type = type;
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
