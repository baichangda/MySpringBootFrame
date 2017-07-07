package com.base.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/5/25.
 */
public class SpringUtil implements ApplicationContextAware{

    private static ApplicationContext applicationContext;


    /**
     * 获取JPA接口 实体类 泛型类型
     * @param obj
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Class getSimpleJpaRepositoryBeanClass(Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(obj==null){
            return null;
        }
        if(!(obj instanceof SimpleJpaRepository)){
            return obj.getClass();
        }
        Method method= SimpleJpaRepository.class.getDeclaredMethod("getDomainClass");
        method.setAccessible(true);
        return (Class)method.invoke(obj);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext=applicationContext;
    }

    /**
     * 获取spring容器中指定类型的bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
}
