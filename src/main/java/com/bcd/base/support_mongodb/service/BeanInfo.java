package com.bcd.base.support_mongodb.service;

import com.bcd.base.support_mongodb.anno.DocumentExt;
import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.bean.BaseBean;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.mongodb.core.mapping.Document;

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
    public final boolean autoSetCreateInfo;
    /**
     * 是否在更新时候自动设置更新信息
     */
    public final boolean autoSetUpdateInfo;


    public BeanInfo(Class<T> clazz) {
        this.clazz = clazz;

        collection = clazz.getAnnotation(Document.class).collection();

        uniqueInfos = Arrays.stream(FieldUtils.getFieldsWithAnnotation(clazz, Unique.class)).map(UniqueInfo::new).toArray(UniqueInfo[]::new);


        if (BaseBean.class.isAssignableFrom(clazz)) {
            DocumentExt documentExt = clazz.getAnnotation(DocumentExt.class);
            if (documentExt == null) {
                autoSetCreateInfo = true;
                autoSetUpdateInfo = true;
            } else {
                autoSetCreateInfo = documentExt.autoSetCreateInfo();
                autoSetUpdateInfo = documentExt.autoSetUpdateInfo();
            }
        } else {
            autoSetCreateInfo = false;
            autoSetUpdateInfo = false;
        }
    }
}
