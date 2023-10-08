package com.bcd.base.support_mongodb.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import java.io.Serializable;

/**
 * 继承此类
 * 因为如果使用业务字段作为主键、直接使用{@link Id}标注即可
 * 需要注意的是、无论主键字段名如何、数据库中的主键字段名总是 _id
 */
@Getter
@Setter
public abstract class SuperBaseBean implements Serializable {
    @Schema(description = "主键(唯一标识符,自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    //主键
    public String id;
}
