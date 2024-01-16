package com.bcd.base.support_mongodb.service;

import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.bean.BaseBean;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class BeanInfo<T> {
    /**
     * service的实体类
     */
    public final Class<T> clazz;

    /**
     * bean所属collection
     */
    public final String collection;

    public final UniqueInfo[] uniqueInfos;

    /**
     * 是否在新增时候自动设置创建信息
     */
    public final boolean isBaseBean;


    public BeanInfo(Class<T> clazz) {
        this.clazz = clazz;

        collection = clazz.getAnnotation(Document.class).collection();

        uniqueInfos = Arrays.stream(FieldUtils.getFieldsWithAnnotation(clazz, Unique.class)).map(UniqueInfo::new).toArray(UniqueInfo[]::new);

        isBaseBean = BaseBean.class.isAssignableFrom(clazz);
    }
}
