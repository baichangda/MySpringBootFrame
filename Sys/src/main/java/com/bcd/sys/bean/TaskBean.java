package com.bcd.sys.bean;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.IPUtil;
import com.bcd.rdb.bean.SuperBaseBean;
import com.bcd.sys.task.TaskStatus;
import com.bcd.sys.task.entity.ClusterTask;
import com.bcd.sys.shiro.ShiroUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  系统任务处理表
 */
@Entity
@Table(name = "t_sys_task")
public class TaskBean extends SuperBaseBean<Long> implements ClusterTask{


    //field
    @Size(max = 100,message = "[关联机构编码]长度不能超过100")
    @ApiModelProperty(value = "关联机构编码(长度不能超过100)")
    private String orgCode;

    @NotBlank(message = "[任务名称]不能为空")
    @Size(max = 50,message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称(不能为空,长度不能超过50)")
    private String name;

    @NotNull(message = "[任务状态]不能为空")
    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)(不能为空)")
    private Integer status;

    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)(不能为空)")
    private Integer type;

    @Size(max = 255,message = "[任务信息]长度不能超过255")
    @ApiModelProperty(value = "任务信息(失败时记录失败原因)(长度不能超过255)")
    private String message;

    @Size(max = 65535,message = "[失败堆栈信息]长度不能超过65535")
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

    @Size(max = 100,message = "[文件路径]长度不能超过100")
    @ApiModelProperty(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)(长度不能超过100)")
    private String filePaths;

    @ApiModelProperty(value = "创建人id")
    private Long createUserId;

    @Size(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)")
    private String createUserName;

    @Size(max = 50,message = "[创建ip]长度不能超过50")
    @ApiModelProperty(value = "创建ip(长度不能超过50)")
    private String createIp;

    @Transient
    private Object[] params;

    @Transient
    private String functionName;

    public TaskBean(String name) {
        this.name=name;
    }

    private TaskBean() {
    }

    //method
    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    @Override
    public void onCreate() {
        createTime=new Date();
        status= TaskStatus.WAITING.getStatus();
        UserBean userBean= ShiroUtil.getCurrentUser();
        if(userBean!=null) {
            createUserId = userBean.getId();
            createUserName = userBean.getRealName();
        }
        createIp= IPUtil.getIpAddress();
    }

    @Override
    public void onStart() {
        startTime=new Date();
        status= TaskStatus.EXECUTING.getStatus();
    }

    @Override
    public void onStop() {
        finishTime=new Date();
        status= TaskStatus.STOPPED.getStatus();
    }

    @Override
    public void onSucceed() {
        finishTime=new Date();
        status= TaskStatus.SUCCEED.getStatus();
    }

    @Override
    public void onFailed(Exception ex) {
        finishTime=new Date();
        status= TaskStatus.FAILED.getStatus();
        Throwable realException= ExceptionUtil.parseRealException(ex);
        message= realException.getMessage();
        stackMessage=ExceptionUtil.getStackTraceMessage(realException);
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public Object[] getParams() {
        return params;
    }
}
