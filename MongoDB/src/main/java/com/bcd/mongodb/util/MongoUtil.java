package com.bcd.mongodb.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Administrator on 2017/11/6.
 */
public class MongoUtil {
    /**
     * 根据JPA的Id注解获取主键值
     *
     * @param obj
     * @return
     */
    public static Object getPKVal(Object obj){
        Class clazz=obj.getClass();
        Field []fields= FieldUtils.getFieldsWithAnnotation(clazz,Id.class);
        if(fields.length==0){
            throw BaseRuntimeException.getException("[MongoUtil.getPKVal],Class["+clazz.getName()+"] Must Have Primary Key!");
        }else if(fields.length>1){
            throw BaseRuntimeException.getException("[MongoUtil.getPKVal],Class["+clazz.getName()+"] Can't Have More Than One Primary Key!");
        }
        Field idField= fields[0];
        try {
            return PropertyUtils.getProperty(obj,idField.getName());
        } catch (IllegalAccessException |InvocationTargetException |NoSuchMethodException  e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
