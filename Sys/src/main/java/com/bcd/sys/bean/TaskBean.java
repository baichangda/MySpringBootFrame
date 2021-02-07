package com.bcd.sys.bean;

import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.IPUtil;
import com.bcd.rdb.bean.SuperBaseBean;
import com.bcd.sys.shiro.ShiroUtil;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 系统任务处理表
 */
@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "t_sys_task")
public class TaskBean extends SuperBaseBean<Long> implements Task {


    //field
    @NotBlank(message = "[任务名称]不能为空")
    @Size(max = 50, message = "[任务名称]长度不能超过50")
    @ApiModelProperty(value = "任务名称(不能为空,长度不能超过50)")
    private String name;

    @NotNull(message = "[任务状态]不能为空")
    @ApiModelProperty(value = "任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)(不能为空)")
    private Integer status;

    @ApiModelProperty(value = "任务类型(1:普通任务;2:文件类型任务)(不能为空)")
    private Integer type;

    @Size(max = 255, message = "[任务信息]长度不能超过255")
    @ApiModelProperty(value = "任务信息(失败时记录失败原因)(长度不能超过255)")
    private String message;

    @NotNull(message = "[任务处理进度]不能为空")
    @ApiModelProperty(value = "任务处理进度")
    private Float percent;

    @Size(max = 65535, message = "[失败堆栈信息]长度不能超过65535")
    @ApiModelProperty(hidden = true, readOnly = true, value = "失败堆栈信息(失败时后台异常堆栈信息)(长度不能超过65535)")
    @Column(columnDefinition = "TEXT")
    @Lazy
    @JsonIgnore
    private String stackMessage;

    @ApiModelProperty(value = "任务开始时间")
    private Date startTime;

    @ApiModelProperty(value = "任务完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Size(max = 100, message = "[文件路径]长度不能超过100")
    @ApiModelProperty(value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)(长度不能超过100)")
    private String filePaths;

    @ApiModelProperty(value = "创建人id")
    private Long createUserId;

    @Size(max = 50, message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)")
    private String createUserName;

    @Size(max = 50, message = "[创建ip]长度不能超过50")
    @ApiModelProperty(value = "创建ip(长度不能超过50)")
    private String createIp;

    public TaskBean(String name) {
        this.name = name;
        this.percent = 0F;
    }

    public TaskBean() {

    }

    @Override
    public void onCreated() {
        createTime = new Date();
        status = TaskStatus.WAITING.getStatus();
        UserBean userBean = ShiroUtil.getCurrentUser();
        if (userBean != null) {
            createUserId = userBean.getId();
            createUserName = userBean.getRealName();
        }
        createIp = IPUtil.getIpAddress();
    }

    @Override
    public void onStarted() {
        startTime = new Date();
        status = TaskStatus.EXECUTING.getStatus();
    }

    @Override
    public void onSucceed() {
        finishTime = new Date();
        percent = 100F;
        status = TaskStatus.SUCCEED.getStatus();
    }

    @Override
    public void onFailed(Exception ex) {
        finishTime = new Date();
        status = TaskStatus.FAILED.getStatus();
        Throwable realException = ExceptionUtil.parseRealException(ex);
        message = ExceptionUtil.getMessage(realException);
        stackMessage = ExceptionUtil.getStackTraceMessage(realException);
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void onStopped() {
        finishTime = new Date();
    }
}
