package com.bcd.rdb.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.*;
import java.lang.reflect.*;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/12.
 */
@SuppressWarnings("unckecked")
public class RDBUtil {

    /**
     * 根据字段名称生成参数名称
     * @param fieldName
     * @param paramMap
     * @return
     */
    public static String generateRandomParamName(String fieldName,Map<String,Object> paramMap){
        String paramName;
        if(fieldName.contains(".")){
            paramName=fieldName.substring(fieldName.indexOf('.')+1);
        }else{
            paramName=fieldName;
        }
        while (paramMap.containsKey(paramName)){
            paramName= fieldName+"_"+ RandomStringUtils.randomNumeric(4);
        }
        return paramName;
    }


    /**
     * 根据JPA的Id注解获取主键值
     *
     * @param obj
     * @return
     */
    public static Object getPKVal(Object obj){
        Class clazz=obj.getClass();

        Field[] fields= FieldUtils.getFieldsWithAnnotation(obj.getClass(),Id.class);
        if(fields.length==0){
            throw BaseRuntimeException.getException("[RDBUtil.getPKVal],Class["+clazz.getName()+"] Must Have Primary Key!");
        }else if(fields.length>1){
            throw BaseRuntimeException.getException("[RDBUtil.getPKVal],Class["+clazz.getName()+"] Can't Have More Than One Primary Key!");
        }
        Field idField= fields[0];
        try {
            return PropertyUtils.getProperty(obj,idField.getName());
        } catch (IllegalAccessException |InvocationTargetException | NoSuchMethodException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取JPA接口 实体类 泛型类型
     * @param obj
     * @return
     */
    public static Class getSimpleJpaRepositoryBeanClass(Object obj){
        if(obj==null){
            return null;
        }
        if(!(obj instanceof SimpleJpaRepository)){
            return obj.getClass();
        }
        Method method=MethodUtils.getAccessibleMethod(SimpleJpaRepository.class,"getDomainClass",Void.class);
        method.setAccessible(true);
        try {
            return (Class) method.invoke(obj);
        } catch (Exception e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
