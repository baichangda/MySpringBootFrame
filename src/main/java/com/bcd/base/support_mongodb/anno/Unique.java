package com.bcd.base.support_mongodb.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 唯一值验证注解
 * 在调用
 * {@link com.bcd.base.support_mongodb.service.BaseService#save(Object)}
 * {@link com.bcd.base.support_mongodb.service.BaseService#save(Iterable)}
 * 时候会进行验证
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
    /**
     * 当存在重复值数据时候返回的错误信息
     * 可以使用如下变量
     * {} 代表字段名称
     */
    String msg() default "字段[{}]值重复";
}