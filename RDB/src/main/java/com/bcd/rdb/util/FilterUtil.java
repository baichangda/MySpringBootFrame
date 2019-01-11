package com.bcd.rdb.util;

import com.bcd.base.define.CommonConst;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.json.SimpleFilterBean;
import com.bcd.base.util.CollectionUtil;
import com.bcd.base.util.SpringUtil;
import com.bcd.rdb.bean.info.BeanInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class FilterUtil {

    private final static Logger logger= LoggerFactory.getLogger(FilterUtil.class);

    public static Cache getBeanFilterCache(){
        if(SpringUtil.applicationContext==null){
            return null;
        }
        return SpringUtil.applicationContext.getBean("jsonFilterCache",Cache.class);
    }

    /**
     * 深度为1的过滤器
     * 根据当前对象类型：
     * 1.将当前对象中带ManyToMany和OneToMany注解的属性获取,这些属性的对象类型为集合类型,
     *   然后根据集合里的参数对象类型得到此对象类型的简单属性过滤器；
     * 2.将当前对象中带ManyToOne和OneToOne注解的属性获取,获取这些类型的深度为0属性过滤器
     *
     * remarks:如果遇到自关联属性、则忽略当前属性过滤器; 如果遇到OneToOne双向关联,则过滤反关联
     *
     * @param clazz
     * @return
     */
    public static SimpleFilterBean[] getOneDeepJsonFilter(Class clazz){
        //1、先从缓存中取
        String cacheKey="ONE_"+clazz.getName();
        Cache cache= getBeanFilterCache();
        SimpleFilterBean[] filters=null;
        if(cache!=null){
            filters=cache.get(cacheKey,SimpleFilterBean[].class);
        }
        if(filters==null){
            BeanInfo beanInfo= BeanInfo.getBeanInfo(clazz);
            List<SimpleFilterBean> SimpleFilterBeans = new ArrayList<>();
            //2、获取当前类型所有带ManyToMany和OneToMany注解的属性集合
            List<Field> collectionFieldList= CollectionUtil.mergeList(null,ArrayList::new,beanInfo.manyToManyFieldList,beanInfo.oneToManyFieldList);
            //3、获取当前类型所有带ManyToOne的注解的属性集合
            List<Field> manyToOneFieldList = beanInfo.manyToOneFieldList;
            //4、获取当前类型所有带OneToOne的注解的属性集合
            List<Field> oneToOneFieldList = beanInfo.oneToOneFieldList;
            //5、遍历所有带ManyToMany和OneToMany注解的属性集合
            for (Field collectionField: collectionFieldList){
                //5.1、拿到属性为集合的类型
                Type genericType = collectionField.getGenericType();
                //5.2、得到该集合里参数对象的类型
                Class fieldClazz = (Class)(((ParameterizedType) genericType).getActualTypeArguments()[0]);
                //5.3、如果遇到自关联属性、则忽略当前属性过滤器
                if(fieldClazz==clazz){
                    continue;
                }
                //5.4、获取集合里参数对象类型的简单属性过滤器，放入SimpleFilterBeanList中
                SimpleFilterBeans.addAll(Arrays.asList(getSimpleJsonFilter(fieldClazz)));
            }
            //6、遍历所有带ManyToOne的注解的属性集合
            if(manyToOneFieldList!=null&&!manyToOneFieldList.isEmpty()){
                manyToOneFieldList.forEach(objectField -> {
                    //6.1、得到该属性的对象类型
                    Class fieldClazz = objectField.getType();
                    //6.2、如果遇到自关联属性、则忽略当前属性过滤器
                    if(fieldClazz==clazz){
                        return;
                    }
                    //6.3、添加简单属性过滤器`
                    SimpleFilterBeans.addAll(Arrays.asList(getZeroDeepJsonFilter(fieldClazz)));
                });
            }
            //7、遍历所有带OneToOne的注解的属性集合
            if(oneToOneFieldList!=null&&!oneToOneFieldList.isEmpty()) {
                oneToOneFieldList.forEach(objectField -> {
                    //7.1、得到该属性的对象类型
                    Class fieldClazz = objectField.getType();
                    //7.2、如果遇到自关联属性、则忽略当前属性过滤器
                    if (fieldClazz == clazz) {
                        return;
                    }
                    //7.3、添加简单属性过滤器`
                    SimpleFilterBeans.addAll(Arrays.asList(getZeroDeepJsonFilter(fieldClazz, clazz)));
                });
            }
            //8、合并多次调用的返回结果,将相同类的filter整合在一起
            Map<String,SimpleFilterBean> filterMap= SimpleFilterBeans.stream().collect(Collectors.toMap(
                    (filter)->filter.getClazz().getName(),
                    (filter)->filter,
                    (filter1,filter2)->{
                        filter1.getExcludes().addAll(filter2.getExcludes());
                        return filter1;
                    }
            ));
            filters= filterMap.values().toArray(new SimpleFilterBean[filterMap.size()]);
            //9、加入到缓存中
            if(cache!=null){
                cache.put(cacheKey,filters);
            }
        }
        return filters;


    }

    /**
     * 过滤掉所有 ManyToOne、OneToOne、ManyToMany、OneToMany注解的属性
     * @param clazz
     * @return
     */
    public static SimpleFilterBean[] getSimpleJsonFilter(Class clazz){
        //1、先从缓存中取
        String cacheKey="SIMPLE_"+clazz.getName();
        Cache cache= getBeanFilterCache();
        SimpleFilterBean[] filters=null;
        if(cache!=null){
            filters= cache.get(cacheKey,SimpleFilterBean[].class);
        }
        //2、如果为空,则生成缓过滤器
        if(filters==null) {
            BeanInfo beanInfo= BeanInfo.getBeanInfo(clazz);
            SimpleFilterBean filter = new SimpleFilterBean(clazz);
            List<Field> fieldList = CollectionUtil.mergeList(null,ArrayList::new,beanInfo.manyToManyFieldList,beanInfo.oneToManyFieldList,beanInfo.manyToOneFieldList,beanInfo.oneToOneFieldList);
            fieldList.forEach(field ->
                filter.getExcludes().add(field.getName())
            );
            filters=new SimpleFilterBean[]{filter};
            //3、加入到缓存中
            if(cache!=null){
                cache.put(cacheKey,filters);
            }
        }
        return filters;
    }

    public static SimpleFilterBean[] getZeroDeepJsonFilter(Class clazz){
        //1、先从缓存中取
        String cacheKey="ZERO_"+clazz.getName();
        Cache cache= getBeanFilterCache();
        SimpleFilterBean[] filters=null;
        if(cache!=null){
            filters= cache.get(cacheKey,SimpleFilterBean[].class);
        }
        //2、如果为空,则生成缓过滤器
        if(filters==null){
            filters=getZeroDeepJsonFilter(clazz,null);
            //3、加入到缓存中
            if(cache!=null){
                cache.put(cacheKey,filters);
            }
        }
        return filters;
    }

    /**
     * 深度为0的过滤器,此过滤器的目的是只留下当前对象的简单属性以及带ManyToOne和OneToOne引用关系的属性
     * 根据当前对象类型：
     * 1、将当前对象类型中带ManyToMany和OneToMany注解的属性过滤；
     * 2、将当前对象类型中带ManyToOne和OneToOne的属性的获取,获取这些类型的简单属性过滤器
     *
     * remarks:如果遇到自关联属性、则忽略当前属性过滤器
     *
     * @param clazz
     * @param parentClass 解决在被 getOneDeepJsonFilter调用 OneToOne 双向关联的问题
     * @return
     */
    public static SimpleFilterBean[] getZeroDeepJsonFilter(Class clazz,Class parentClass){
        BeanInfo beanInfo= BeanInfo.getBeanInfo(clazz);
        List<SimpleFilterBean> SimpleFilterBeans = new ArrayList<>();
        //1、获取当前类型所有带ManyToMany和OneToMany注解的属性集合
        List<Field> collectionFieldList = CollectionUtil.mergeList(null,ArrayList::new,beanInfo.manyToManyFieldList,beanInfo.oneToManyFieldList);
        //2、获取当前类型所有带ManyToOne和OneToOne的注解的属性集合
        List<Field> objectFieldList = CollectionUtil.mergeList(null,ArrayList::new,beanInfo.manyToOneFieldList,beanInfo.oneToOneFieldList);
        SimpleFilterBean collectionFilter=new SimpleFilterBean(clazz);
        //3.遍历所有带ManyToMany和OneToMany注解的属性集合,将属性名称放入过滤器
        collectionFieldList.forEach(collectionField -> {
            collectionFilter.getExcludes().add(collectionField.getName());
        });
        SimpleFilterBeans.add(collectionFilter);
        //4.遍历所有带ManyToOne和OneToOne的注解的属性集合
        objectFieldList.forEach(objectField -> {
            //4.1、得到该属性的对象类型
            Class fieldClazz = objectField.getType();
            //4.2、如果遇到自关联属性、则忽略当前属性过滤器
            if(fieldClazz==clazz){
                return;
            }
            //4.3、如果遇到父类的class、则排除这个属性
            if(parentClass!=null&&fieldClazz==parentClass){
                collectionFilter.getExcludes().add(objectField.getName());
                return;
            }
            //4.4、得到该属性的对象类型的简单属性过滤器
            SimpleFilterBeans.addAll(Arrays.asList(getSimpleJsonFilter(fieldClazz)));
        });
        //5、合并多次调用的返回结果,将相同类的filter整合在一起
        Map<String,SimpleFilterBean> filterMap= SimpleFilterBeans.stream().collect(Collectors.toMap(
                (filter)->filter.getClazz().getName(),
                (filter)->filter,
                (filter1,filter2)->{
                    filter1.getExcludes().addAll(filter2.getExcludes());
                    return filter1;
                }
        ));
        return filterMap.values().toArray(new SimpleFilterBean[filterMap.size()]);
    }

    /**
     * 递归深入对象层级中，将指定层级的引用设置为null，避免转json时候循环引用
     * @param obj 对象 可以是 obj,list,map
     * @param level 指定的引用对象的保留层级
     * @param isJpaField 是否只是处理jpa注解的字段
     */
    public static void clear(Object obj, int level, boolean isJpaField) {
        if (obj == null) {
            return;
        }
        Class clazz = obj.getClass();

        Iterable iterable;
        if (Iterable.class.isAssignableFrom(clazz)) {
            iterable = (Iterable) obj;
        } else if (Map.class.isAssignableFrom(clazz)) {
            iterable = ((Map) obj).values();
        } else {
            iterable = Arrays.asList(obj);
        }
        iterable.forEach(o -> {
            List<Field> fieldList;
            if (isJpaField) {
                BeanInfo beanInfo= BeanInfo.getBeanInfo(clazz);
                fieldList = CollectionUtil.mergeList(null,ArrayList::new,beanInfo.manyToManyFieldList,beanInfo.oneToManyFieldList,beanInfo.manyToOneFieldList,beanInfo.oneToOneFieldList);
            }else{
                fieldList=FieldUtils.getAllFieldsList(o.getClass()).stream().filter(e->{
                    for (Class aClass : CommonConst.BASE_DATA_TYPE) {
                        if(aClass.isAssignableFrom(e.getType())){
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            if (level == 0) {
                try {
                    for (Field field : fieldList) {
                        BeanUtils.setProperty(o,field.getName(),null);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw BaseRuntimeException.getException(e);
                }
            } else {
                try {
                    for (Field field : fieldList) {
                        clear(PropertyUtils.getProperty(o, field.getName()), level - 1,isJpaField);
                    }
                } catch (IllegalAccessException |InvocationTargetException |NoSuchMethodException e) {
                    logger.error("Error",e);
                }
            }
        });
    }
}
