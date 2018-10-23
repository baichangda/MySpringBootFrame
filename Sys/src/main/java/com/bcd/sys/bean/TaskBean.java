package com.bcd.sys.bean;

import com.bcd.rdb.bean.SuperBaseBean;
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
    @ApiModelProperty(position = 1, value = "任务名称")
    private String name;

    @NotNull(message = "任务状态不能为空")
    @ApiModelProperty(position = 2, value = "任务状态(1:等待中;2:执行中;2:任务被终止;2:已完成;3:执行失败;)")
    private Integer status;

    @NotNull(message = "任务类型不能为空")
    @ApiModelProperty(position = 3, value = "任务类型(1:普通任务;2:文件类型任务)")
    private Byte type;

    @NotBlank(message = "备注不能为空")
    @Length(max = 255,message = "[备注]长度不能超过255")
    @ApiModelProperty(position = 4, value = "备注(失败时记录失败原因)")
    private String remark;

    @ApiModelProperty(position = 5, value = "任务完成时间")
    private Date finishTime;

    @ApiModelProperty(position = 6, value = "创建时间")
    private Date createTime;

    @NotBlank(message = "文件路径不能为空")
    @Length(max = 100,message = "[文件路径]长度不能超过100")
    @ApiModelProperty(position = 7, value = "文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)")
    private String filePaths;

    @ApiModelProperty(position = 8, value = "创建人id")
    private Long createUserId;

    @Length(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(position = 9, value = "创建人姓名")
    private String createUserName;

    @Length(max = 50,message = "[创建ip]长度不能超过50")
    @ApiModelProperty(position = 10, value = "创建ip")
    private String createIp;


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

    public void setType(Byte type){
        this.type=type;
    }

    public Byte getType(){
        return this.type;
    }

    public void setRemark(String remark){
        this.remark=remark;
    }

    public String getRemark(){
        return this.remark;
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


}
