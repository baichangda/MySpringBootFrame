package com.bcd.base.support_jpa.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_jpa.bean.info.BeanInfo;
import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.base.support_jpa.util.ConditionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public class BaseService<T, K extends Serializable> {
    @PersistenceContext
    public EntityManager em;

    @Autowired(required = false)
    public BaseRepository<T, K> repository;


    private BeanInfo beanInfo;


    /**
     * 获取当前service对应实体类的信息
     *
     * @return
     */
    public final BeanInfo getBeanInfo() {
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

    public boolean existsById(K id) {
        return repository.existsById(id);
    }

    public long count() {
        return repository.count();
    }

    public long count(Condition condition) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.count(specification);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAll(Condition condition) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.findAll(specification);
    }

    public List<T> findAll(Condition condition, Sort sort) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.findAll(specification, sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> findAll(Condition condition, Pageable pageable) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.findAll(specification, pageable);
    }

    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public List<T> findAllById(Iterable<K> iterable) {
        return repository.findAllById(iterable);
    }

    public List<T> findAllById(K... kArr) {
        return repository.findAllById(Arrays.asList(kArr));
    }

    public T findById(K k) {
        return repository.findById(k).orElse(null);
    }


    public T findOne(Condition condition) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.findOne(specification).orElse(null);
    }


    public T save(T t) {
        return repository.save(t);
    }

    public List<T> saveAll(Iterable<T> iterable) {
        return repository.saveAll(iterable);
    }


    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteById(K id) {
        repository.deleteById(id);
    }

    public void delete(T t) {
        repository.delete(t);
    }

    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    public void deleteAllById(Iterable<? extends K> ids) {
        repository.deleteAllById(ids);
    }

    public void deleteAllById(K ...ids) {
        repository.deleteAllById(Arrays.asList(ids));
    }
}
