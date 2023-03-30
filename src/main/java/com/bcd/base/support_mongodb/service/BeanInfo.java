package com.bcd.base.support_mongodb.service;

import com.bcd.base.support_mongodb.anno.Unique;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class BeanInfo<T> {
    /**
     * service的实体类
     */
    public final Class<T> clazz;

    /**
     * 主键字段
     */
    public final Field pkField;
    public final String pkFieldName;

    /**
     * bean所属collection
     */
    public final String collection;

    public final Field[] uniqueFields;


    public BeanInfo(Class<T> clazz) {
        this.clazz = clazz;

        pkField = FieldUtils.getFieldsWithAnnotation(clazz, Id.class)[0];
        pkField.setAccessible(true);
        pkFieldName = pkField.getName();

        collection = clazz.getAnnotation(Document.class).collection();

        uniqueFields = FieldUtils.getFieldsWithAnnotation(clazz, Unique.class);
    }
}
