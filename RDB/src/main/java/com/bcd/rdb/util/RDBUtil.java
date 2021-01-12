package com.bcd.rdb.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.commons.beanutils.MethodUtils;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.lang.reflect.*;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/12.
 */
@SuppressWarnings("unckecked")
public class RDBUtil {

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
