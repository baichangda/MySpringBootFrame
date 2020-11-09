package com.bcd.rdb.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/11.
 */
@Accessors(chain = true)
@Getter
@Setter
@MappedSuperclass
public class BaseBean<K extends Serializable> extends SuperBaseBean<K> {

    @ApiModelProperty(value = "创建时间(不需要赋值)")
    protected Date createTime;

    @ApiModelProperty(value = "创建人id(不需要赋值)")
    protected K createUserId;

    @Length(max = 50,message = "[创建人姓名]长度不能超过50")
    @ApiModelProperty(value = "创建人姓名(长度不能超过50)(不需要赋值)")
    protected String createUserName;

    @ApiModelProperty(value = "更新时间(不需要赋值)")
    protected Date updateTime;

    @ApiModelProperty(value = "更新人id(不需要赋值)")
    protected K updateUserId;

    @Length(max = 50,message = "[更新人姓名]长度不能超过50")
    @ApiModelProperty(value = "更新人姓名(长度不能超过50)(不需要赋值)")
    protected String updateUserName;

    @Size
    @Length(max = 50,message = "[创建ip地址]长度不能超过50")
    @ApiModelProperty(value = "创建ip地址(长度不能超过50)(不需要赋值)")
    protected String createIp;

    @Length(max = 50,message = "[更新ip地址]长度不能超过50")
    @ApiModelProperty(value = "更新ip地址(长度不能超过50)(不需要赋值)")
    protected String updateIp;
}
