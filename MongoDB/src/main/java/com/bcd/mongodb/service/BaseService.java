package com.bcd.mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.i18n.I18NData;
import com.bcd.mongodb.anno.Unique;
import com.bcd.mongodb.bean.info.BeanInfo;
import com.bcd.mongodb.repository.BaseRepository;
import com.bcd.mongodb.util.ConditionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.lang.reflect.Field;
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

    private volatile BeanInfo beanInfo;

    public BeanInfo getBeanInfo() {
        if (beanInfo == null) {
            synchronized (this) {
                if (beanInfo == null) {
                    Class beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    beanInfo = new BeanInfo(beanClass);
                }
            }
        }
        return beanInfo;
    }

    public List<T> findAll(){
        return repository.findAll();
    }

    public List<T> findAll(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query,getBeanInfo().clazz);
    }

    public List<T> findAll(Sort sort){
        return repository.findAll(sort);
    }

    public List<T> findAll(Condition condition,Sort sort){
        Query query= ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query,getBeanInfo().clazz);
    }

    public Page<T> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    public Page<T> findAll(Condition condition, Pageable pageable){
        Query query= ConditionUtil.toQuery(condition);
        long count=mongoTemplate.count(query,getBeanInfo().clazz);
        query.with(pageable);
        List<T> list=mongoTemplate.find(query,getBeanInfo().clazz);
        return new PageImpl<>(list, pageable, count);
    }

    public long count(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        return mongoTemplate.count(query,getBeanInfo().clazz);
    }

    public T findById(K id){
        return repository.findById(id).orElse(null);
    }

    public T findOne(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query,(Class<T>)getBeanInfo().clazz);
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
        //1、循环集合,验证每个唯一字段是否在数据库中有重复值
        try {
            for (Field f : getBeanInfo().uniqueFieldList) {
                Object val= f.get(t);
                if(!isUnique(f.getName(),val,(K) getBeanInfo().pkField.get(t))){
                    throw BaseRuntimeException.getException(getUniqueMessage(f));
                }
            }
        } catch (IllegalAccessException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 保存前进行批量唯一性验证
     * @param iterable
     */
    public void validateUniqueBeforeSave(Iterable<T> iterable){
        //1、循环集合,看传入的参数集合中唯一字段是否有重复的值
        try {
        Map<String,Set<Object>> fieldValueSetMap=new HashMap<>();
            for (T t : iterable) {
                for (Field f : getBeanInfo().uniqueFieldList) {
                    String fieldName=f.getName();
                    Object val= f.get(t);
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
                for (Field f : getBeanInfo().uniqueFieldList) {
                    Object val=f.get(t);
                    if(!isUnique(f.getName(),val,(K)getBeanInfo().pkField.get(t))){
                        throw BaseRuntimeException.getException(getUniqueMessage(f));
                    }
                }
            }
        } catch (IllegalAccessException e) {
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

    public void deleteById(K[] ids){
        Object[] newIds=new Object[ids.length];
        System.arraycopy(ids,0,newIds,0,ids.length);
        Query query=new Query(Criteria.where(getBeanInfo().pkFieldName).in(newIds));
        mongoTemplate.remove(query,getBeanInfo().clazz);
//        for(int i=0;i<=ids.length-1;i++){
//            repository.deleteById(ids[i]);
//        }
    }

    public void deleteAll(){
        repository.deleteAll();
    }

    public void delete(Condition condition){
        Query query= ConditionUtil.toQuery(condition);
        mongoTemplate.remove(query,getBeanInfo().clazz);
    }

    /**
     * 字段唯一性验证
     * @param fieldName 属性名称
     * @param val 属性值
     * @param excludeIds 排除id数组
     * @return
     */
    public boolean isUnique(String fieldName,Object val,K ... excludeIds){
        Query query=new Query(Criteria.where(fieldName).is(val));
        List<T> resultList= mongoTemplate.find(query,getBeanInfo().clazz);
        if(excludeIds==null||excludeIds.length==0){
            return resultList.isEmpty();
        }else{
            Set<K> idSet= Arrays.stream(excludeIds).collect(Collectors.toSet());
            return resultList.stream().allMatch(e-> {
                try {
                    return idSet.contains(getBeanInfo().pkField.get(e));
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
        }
    }
}
