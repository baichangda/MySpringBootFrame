package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
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

    public List<T> list() {
        return repository.findAll();
    }

    public List<T> list(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public List<T> list(Sort sort) {
        return repository.findAll(sort);
    }

    public List<T> list(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public Page<T> page(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> page(Condition condition, Pageable pageable) {
        Query query = ConditionUtil.toQuery(condition);
        final long total = mongoTemplate.count(query, beanInfo.clazz);
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (total > offset) {
            query.with(pageable);
            List<T> list = mongoTemplate.find(query, beanInfo.clazz);
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, total);
        }
    }

    public long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.count(query, beanInfo.clazz);
    }

    public T get(String id) {
        return repository.findById(id).orElse(null);
    }

    public T get(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query, (Class<T>) beanInfo.clazz);
    }

    public T save(T t) {
        return repository.save(t);
    }

    public List<T> save(Iterable<T> iterable) {
        return repository.saveAll(iterable);
    }

    /**
     * 删除所有数据
     */
    public void delete() {
        repository.deleteAll();
    }

    /**
     * 根据id删除
     *
     * @param ids
     */
    public void delete(String... ids) {
        if (ids.length == 1) {
            repository.deleteById(ids[0]);
        } else if (ids.length > 1) {
            Object[] newIds = new Object[ids.length];
            System.arraycopy(ids, 0, newIds, 0, ids.length);
            Query query = new Query(Criteria.where(beanInfo.pkFieldName).in(newIds));
            mongoTemplate.remove(query, beanInfo.clazz);
        }
    }

    /**
     * 根据条件删除
     *
     * @param condition
     */
    public void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        mongoTemplate.remove(query, beanInfo.clazz);
    }
}
