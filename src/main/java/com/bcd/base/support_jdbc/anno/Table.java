package com.bcd.base.support_jdbc.anno;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_jdbc.bean.SuperBaseBean;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 表名
     */
    String value();

    /**
     * 此属性指定是否在新增时候自动设置创建信息
     * 默认开启
     * 同时满足如下3条件才会自动设置
     * 1、当bean继承{@link com.bcd.base.support_jdbc.bean.BaseBean}时候才有效
     * 2、此属性设置为true
     *
     * 影响的方法如下
     * {@link com.bcd.base.support_jdbc.service.BaseService#insert(SuperBaseBean)}
     * {@link com.bcd.base.support_jdbc.service.BaseService#insert(Map)}
     * {@link com.bcd.base.support_jdbc.service.BaseService#insertBatch(List)}
     */
    boolean autoSetCreateInfoBeforeInsert() default true;

    /**
     * 此属性指定是否在更新时候自动设置更新信息
     * 默认开启
     * 同时满足如下3条件才会自动设置
     * 1、当bean继承{@link com.bcd.base.support_jdbc.bean.BaseBean}时候才有效
     * 2、此属性设置为true
     *
     * 影响的方法如下
     * {@link com.bcd.base.support_jdbc.service.BaseService#update(SuperBaseBean)}
     * {@link com.bcd.base.support_jdbc.service.BaseService#update(long, Map)}
     * {@link com.bcd.base.support_jdbc.service.BaseService#update(Condition, Map)}
     * {@link com.bcd.base.support_jdbc.service.BaseService#updateBatch(List)}
     */
    boolean autoSetUpdateInfoBeforeUpdate() default true;
}
