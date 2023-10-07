package com.bcd.sys.bean;

import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.bean.SuperBaseBean;
import com.bcd.base.support_satoken.SaTokenUtil;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.support_task.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * 系统任务处理表
 */
@Getter
@Setter
@Table("t_sys_task")
public class TaskBean implements Task<Long>,SuperBaseBean<Long> {
    @Schema(description = "主键")
    @Id
    //主键
    public Long id;

    //field
    @NotBlank(message = "[任务名称]不能为空")
    @Size(max = 50, message = "[任务名称]长度不能超过50")
    @Schema(description = "任务名称", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    public String name;

    @NotNull(message = "[任务状态]不能为空")
    @Schema(description = "任务状态(1:等待中;2:执行中;3:执行成功;4:执行失败;5:任务被取消;:6:任务被终止)", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer status;

    @Schema(description = "任务类型(1:普通任务;2:文件类型任务)", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer type;

    @Size(max = 255, message = "[任务信息]长度不能超过255")
    @Schema(description = "任务信息(失败时记录失败原因)", maxLength = 255)
    public String message;

    @NotNull(message = "[任务处理进度]")
    @Schema(description = "任务处理进度", requiredMode = Schema.RequiredMode.REQUIRED)
    public float percent;

    @Size(max = 65535, message = "[失败堆栈信息]长度不能超过65535")
    @Schema(hidden = true, description = "失败堆栈信息(失败时后台异常堆栈信息)", maxLength = 65535)
    @Lazy
    @JsonIgnore
    public String stackMessage;

    @Schema(description = "任务开始时间")
    public Date startTime;

    @Schema(description = "任务完成时间")
    public Date finishTime;

    @Schema(description = "创建时间")
    public Date createTime;

    @Size(max = 100, message = "[文件路径]长度不能超过100")
    @Schema(description = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)", maxLength = 100)
    public String filePaths;

    @Schema(description = "创建人id")
    public Long createUserId;

    @Size(max = 50, message = "[创建人姓名]长度不能超过50")
    @Schema(description = "创建人姓名", maxLength = 50)
    public String createUserName;

    public TaskBean(String name) {
        this.name = name;
    }

    public TaskBean() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void onCreated() {
        createTime = new Date();
        UserBean userBean = (UserBean) SaTokenUtil.getLoginUser_cache();
        if (userBean != null) {
            createUserId = userBean.getId();
            createUserName = userBean.realName;
        }
    }

    @Override
    public void onStarted() {
        startTime = new Date();
    }

    @Override
    public void onSucceed() {
        finishTime = new Date();
        percent = 100F;
    }

    @Override
    public void onFailed(Exception ex) {
        finishTime = new Date();
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

    @Override
    public Long getId() {
        return id;
    }
}
