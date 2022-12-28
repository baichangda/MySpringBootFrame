package com.bcd.base.support_jdbc.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@Getter
@Setter
public abstract class SuperBaseBean implements Serializable {
    @Schema(description = "主键(唯一标识符,自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    //主键
    public Long id;
}
