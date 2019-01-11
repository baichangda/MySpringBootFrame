package com.bcd.mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.i18n.I18NData;
import com.bcd.mongodb.anno.Unique;
import com.bcd.mongodb.repository.BaseRepository;
import com.bcd.mongodb.util.ConditionUtil;
import com.bcd.mongodb.util.MongoUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class BaseService<T,K extends Serializable>{
    @Autowired
    public MongoTemplate mongoTemplate;
    @Autowired
    public BaseRepository<T,K> repository;

    public Class<T> beanClass;

    public List<Field> uniqueFieldList;

    @PostConstruct
    public void initBeanClass() {
        this.beanClass = (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public List<T> findAll(){
        return repository.findAll();
    }

    public List<T> findAll(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query,beanClass);
    }

    public List<T> findAll(Sort sort){
        return repository.findAll(sort);
    }

    public List<T> findAll(Condition condition,Sort sort){
        Query query= ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query,beanClass);
    }

    public Page<T> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    public Page<T> findAll(Condition condition, Pageable pageable){
        Query query= ConditionUtil.toQuery(condition);
        query.with(pageable);
        long count=repository.count();
        List<T> list=mongoTemplate.find(query,beanClass);
        return new PageImpl(list, pageable, count);
    }

    public T findById(K id){
        return repository.findById(id).orElse(null);
    }

    public T findOne(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query,beanClass);
    }

    /**
     * 获取唯一注解字段的message值
     * @param field
     * @return
     */
    private String getUniqueMessage(Field field){
        Unique anno= field.getAnnotation(Unique.class);
        String msg=anno.messageValue();
        if(StringUtils.isEmpty(msg)){
            msg= I18NData.getI18NData(anno.messageKey()).getValue(field.getName());
        }
        return msg;
    }

    /**
     * 保存前进行唯一性验证
     * @param t
     */
    public void validateUniqueBeforeSave(T t){
        //1、获取唯一注解的字段集合
        if(uniqueFieldList==null){
            uniqueFieldList= Arrays.asList(FieldUtils.getFieldsWithAnnotation(beanClass, Unique.class));
        }
        //2、循环集合,验证每个唯一字段是否在数据库中有重复值
        try {
            for (Field f : uniqueFieldList) {
                Object val= PropertyUtils.getProperty(t,f.getName());
                if(!isUnique(f.getName(),val,(K) MongoUtil.getPKVal(t))){
                    throw BaseRuntimeException.getException(getUniqueMessage(f));
                }
            }
        } catch (IllegalAccessException |InvocationTargetException |NoSuchMethodException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 保存前进行批量唯一性验证
     * @param iterable
     */
    public void validateUniqueBeforeSave(Iterable<T> iterable){
        //1、获取唯一注解的字段集合
        if(uniqueFieldList==null){
            uniqueFieldList= Arrays.asList(FieldUtils.getFieldsWithAnnotation(beanClass, Unique.class));
        }
        //2、循环集合,看传入的参数集合中唯一字段是否有重复的值
        try {
        Map<String,Set<Object>> fieldValueSetMap=new HashMap<>();
            for (T t : iterable) {
                for (Field f : uniqueFieldList) {
                    String fieldName=f.getName();
                    Object val= PropertyUtils.getProperty(t,fieldName);
                    Set<Object> valueSet= fieldValueSetMap.get(fieldName);
                    if(valueSet==null){
                        valueSet=new HashSet<>();
                        fieldValueSetMap.put(fieldName,valueSet);
                    }else{
                        if(valueSet.contains(val)){
                            throw BaseRuntimeException.getException(getUniqueMessage(f));
                        }
                    }
                    valueSet.add(val);
                }
            }
            //3、循环集合,验证每个唯一字段是否在数据库中有重复值
            for (T t : iterable) {
                for (Field f : uniqueFieldList) {
                    String fieldName=f.getName();
                    Object val=PropertyUtils.getProperty(t,fieldName);
                    if(!isUnique(f.getName(),val,(K)MongoUtil.getPKVal(t))){
                        throw BaseRuntimeException.getException(getUniqueMessage(f));
                    }
                }
            }
        } catch (IllegalAccessException |InvocationTargetException |NoSuchMethodException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public T save(T t){
        validateUniqueBeforeSave(t);
        return repository.save(t);
    }

    public List<T> saveAll(Iterable<T> iterable){
        validateUniqueBeforeSave(iterable);
        return repository.saveAll(iterable);
    }

    public void delete(T t){
        repository.delete(t);
    }

    public void deleteAll(Iterable<T> iterable){
        repository.deleteAll(iterable);
    }

    public void deleteById(K id){
        repository.deleteById(id);
    }

    @Transactional
    public void deleteById(K[] ids){
        for(int i=0;i<=ids.length-1;i++){
            repository.deleteById(ids[i]);
        }
    }

    public void deleteAll(){
        repository.deleteAll();
    }

    /**
     * 字段唯一性验证
     * @param fieldName 属性名称
     * @param val 属性值
     * @param excludeIds 排除id数组
     * @return
     */
    public boolean isUnique(String fieldName,Object val,K ... excludeIds){
        boolean flag=true;
        Query query=new Query(Criteria.where(fieldName).is(val));
        List<T> resultList= mongoTemplate.find(query,beanClass);
        if(excludeIds==null||excludeIds.length==0){
            if (resultList!=null&&!resultList.isEmpty()){
                flag= false;
            }
        }else{
            Set<K> idSet= Arrays.stream(excludeIds).collect(Collectors.toSet());
            List filterList=resultList.stream().filter(e->!idSet.contains(MongoUtil.getPKVal(e))).collect(Collectors.toList());
            if (filterList!=null&&!filterList.isEmpty()){
                flag= false;
            }
        }
        return flag;
    }
}
