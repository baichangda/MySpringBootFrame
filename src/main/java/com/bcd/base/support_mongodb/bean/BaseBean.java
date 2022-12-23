package com.bcd.base.support_mongodb.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/25.
 */
public class BaseBean<K extends Serializable> extends SuperBaseBean<K> {
    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    public Date createTime;

    @Schema(description = "创建人id", accessMode = Schema.AccessMode.READ_ONLY)
    public K createUserId;

    @Length(max = 50, message = "[创建人姓名]长度不能超过50")
    @Schema(description = "创建人姓名", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    public String createUserName;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    public Date updateTime;

    @Schema(description = "更新人id", accessMode = Schema.AccessMode.READ_ONLY)
    public K updateUserId;

    @Length(max = 50, message = "[更新人姓名]长度不能超过50")
    @Schema(description = "更新人姓名", maxLength = 50, accessMode = Schema.AccessMode.READ_ONLY)
    public String updateUserName;
}
