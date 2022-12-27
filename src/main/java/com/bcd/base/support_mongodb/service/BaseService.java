package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.util.ConditionUtil;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class BaseService<T> {
    @Autowired
    public MongoTemplate mongoTemplate;
    @Autowired(required = false)
    public BaseRepository<T> repository;

    private final BeanInfo beanInfo;

    public BaseService() {
        final Class beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo(beanClass);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAll(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public List<T> findAll(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> findAll(Condition condition, Pageable pageable) {
        Query query = ConditionUtil.toQuery(condition);
        long count = mongoTemplate.count(query, beanInfo.clazz);
        query.with(pageable);
        List<T> list = mongoTemplate.find(query, beanInfo.clazz);
        return new PageImpl<>(list, pageable, count);
    }

    public long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.count(query, beanInfo.clazz);
    }

    public T findById(String id) {
        return repository.findById(id).orElse(null);
    }

    public T findOne(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query, (Class<T>) beanInfo.clazz);
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

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public void deleteById(String[] ids) {
        Object[] newIds = new Object[ids.length];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        Query query = new Query(Criteria.where(beanInfo.pkFieldName).in(newIds));
        mongoTemplate.remove(query, beanInfo.clazz);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        mongoTemplate.remove(query, beanInfo.clazz);
    }
}
