package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_mongodb.bean.info.BeanInfo;
import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.util.ConditionUtil;
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
public class BaseService<T, K extends Serializable> {
    @Autowired
    public MongoTemplate mongoTemplate;
    @Autowired(required = false)
    public BaseRepository<T, K> repository;

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

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAll(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query, getBeanInfo().clazz);
    }

    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public List<T> findAll(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query, getBeanInfo().clazz);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> findAll(Condition condition, Pageable pageable) {
        Query query = ConditionUtil.toQuery(condition);
        long count = mongoTemplate.count(query, getBeanInfo().clazz);
        query.with(pageable);
        List<T> list = mongoTemplate.find(query, getBeanInfo().clazz);
        return new PageImpl<>(list, pageable, count);
    }

    public long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.count(query, getBeanInfo().clazz);
    }

    public T findById(K id) {
        return repository.findById(id).orElse(null);
    }

    public T findOne(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query, (Class<T>) getBeanInfo().clazz);
    }

    public T save(T t) {
        return repository.save(t);
    }

    public List<T> saveAll(Iterable<T> iterable) {
        return repository.saveAll(iterable);
    }

    public void delete(T t) {
        repository.delete(t);
    }

    public void deleteAll(Iterable<T> iterable) {
        repository.deleteAll(iterable);
    }

    public void deleteById(K id) {
        repository.deleteById(id);
    }

    public void deleteById(K[] ids) {
        Object[] newIds = new Object[ids.length];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        Query query = new Query(Criteria.where(getBeanInfo().pkFieldName).in(newIds));
        mongoTemplate.remove(query, getBeanInfo().clazz);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        mongoTemplate.remove(query, getBeanInfo().clazz);
    }
}
