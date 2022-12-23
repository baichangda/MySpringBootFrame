package com.bcd.base.support_jpa.bean.info;

import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class BeanInfo {
    /**
     * service的实体类
     */
    public final Class clazz;

    /**
     * 实体类表名
     */
    public String tableName;

    /**
     * 主键字段
     */
    public Field pkField;


    public BeanInfo(Class clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        initTableName();
        initPkField();
    }

    private void initTableName() {
        Table table = ((Table) clazz.getAnnotation(Table.class));
        this.tableName = table == null ? null : table.name();
    }

    /**
     * 初始化主键字段
     */
    private void initPkField() {
        pkField = FieldUtils.getFieldsWithAnnotation(clazz, Id.class)[0];
        pkField.setAccessible(true);
    }

}
