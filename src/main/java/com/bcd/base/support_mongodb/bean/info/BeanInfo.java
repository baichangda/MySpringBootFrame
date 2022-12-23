package com.bcd.base.support_mongodb.bean.info;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class BeanInfo {
    /**
     * service的实体类
     */
    public Class clazz;

    /**
     * 主键字段
     */
    public Field pkField;
    public String pkFieldName;

    /**
     * bean所属collection
     */
    public String collection;


    public BeanInfo(Class clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        initPkField();
        initCollection();
    }

    public void initCollection() {
        collection = ((Document) clazz.getAnnotation(Document.class)).collection();
    }


    /**
     * 初始化主键字段
     */
    public void initPkField() {
        pkField = FieldUtils.getFieldsWithAnnotation(clazz, Id.class)[0];
        pkField.setAccessible(true);
        pkFieldName = pkField.getName();
    }

}
