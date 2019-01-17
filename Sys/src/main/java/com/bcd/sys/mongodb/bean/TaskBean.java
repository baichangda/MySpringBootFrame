package com.bcd.sys.mongodb.bean;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.IPUtil;
import com.bcd.mongodb.bean.SuperBaseBean;
import com.bcd.mongodb.code.CodeGenerator;
import com.bcd.mongodb.code.CollectionConfig;
import com.bcd.mongodb.code.Config;
import com.bcd.sys.UserDataAccess;
import com.bcd.sys.shiro.ShiroUtil;
import com.bcd.sys.task.TaskStatus;
import com.bcd.sys.task.entity.ClusterTask;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *  系统任务处理表
 */
@Document(collection = "task")
public class TaskBean extends SuperBaseBean<String> implements ClusterTask{


    //field
    @NotBlank(message = "[任务名称]不能为空")
    @Size(max = 50,message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称(不能为空,长度不能超过50)")
    //任务名称
    private String name;

    @NotNull(message = "[任务状态]不能为空")
    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)(不能为空)")
    //任务状态
    private Integer status;

    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)(不能为空)")
    //任务类型
    private Integer type;

    @Size(max = 255,message = "[任务信息]长度不能超过255")
    @ApiModelProperty(value = "任务信息(失败时记录失败原因)(长度不能超过255)")
    //任务信息
    private String message;

    @Size(max = 65535,message = "[失败堆栈信息]长度不能超过65535")
    @ApiModelProperty(hidden = true,readOnly = true,value = "失败堆栈信息(失败时后台异常堆栈信息)(长度不能超过65535)")
    @Lazy
    @JsonIgnore
    //失败堆栈信息
    private String stackMessage;

    @ApiModelProperty(value = "任务开始时间")
    //任务开始时间
    private Date startTime;

    @ApiModelProperty(value = "任务完成时间")
    //任务完成时间
    private Date finishTime;

    @ApiModelProperty(value = "创建时间")
    //创建时间
    private Date createTime;

    @Size(max = 100,message = "[文件路径]长度不能超过100")
    @ApiModelProperty(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)(长度不能超过100)")
    //文件路径
    private String filePaths;

    @ApiModelProperty(value = "创建人id")
    //创建人id
    private String createUserId;

    @Size(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)")
    //创建人姓名
    private String createUserName;

    @Size(max = 50,message = "[创建ip]长度不能超过50")
    @ApiModelProperty(value = "创建ip(长度不能超过50)")
    //创建人ip
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

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
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
        UserDataAccess user= ShiroUtil.getCurrentUser();
        if(user!=null) {
            createUserId = user.getId().toString();
            createUserName = user.getUsername();
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

    public static void main(String [] args){
        Config configProperties=new Config(
                new CollectionConfig("Task","系统任务", TaskBean.class)
        );
        CodeGenerator.generate(configProperties);
    }
}
