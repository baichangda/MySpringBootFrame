package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.util.ConditionUtil;
import com.bcd.base.util.StringUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final Class<T> beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo<>(beanClass);
    }

    public final List<T> list() {
        return repository.findAll();
    }

    public final List<T> list(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public final List<T> list(Sort sort) {
        return repository.findAll(sort);
    }

    public final List<T> list(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return mongoTemplate.find(query, beanInfo.clazz);
    }

    public final Page<T> page(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public final Page<T> page(Condition condition, Pageable pageable) {
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

    public final long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.count(query, beanInfo.clazz);
    }

    public final T get(String id) {
        return repository.findById(id).orElse(null);
    }

    public final T get(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return mongoTemplate.findOne(query, (Class<T>) beanInfo.clazz);
    }

    public final T save(T t) {
        validateUniqueBeforeSave(Collections.singletonList(t));
        return repository.save(t);
    }

    public final List<T> save(Iterable<T> iterable) {
        validateUniqueBeforeSave(Streams.stream(iterable).toList());
        return repository.saveAll(iterable);
    }

    /**
     * 删除所有数据
     */
    public final void delete() {
        repository.deleteAll();
    }

    /**
     * 根据id删除
     *
     * @param ids
     */
    public final void delete(String... ids) {
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
    public final void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        mongoTemplate.remove(query, beanInfo.clazz);
    }

    private void validateUniqueBeforeSave(List<T> list) {
        if (beanInfo.uniqueFields.length > 0) {
            try {
                //1、循环集合,看传入的参数集合中唯一字段是否有重复的值
                if (list.size() > 1) {
                    Map<String, Set<Object>> fieldValueSetMap = new HashMap<>();
                    for (T t : list) {
                        for (Field f : beanInfo.uniqueFields) {
                            String fieldName = f.getName();
                            Object val = f.get(t);
                            Set<Object> valueSet = fieldValueSetMap.get(fieldName);
                            if (valueSet == null) {
                                valueSet = new HashSet<>();
                                fieldValueSetMap.put(fieldName, valueSet);
                            } else {
                                if (valueSet.contains(val)) {
                                    throw BaseRuntimeException.getException(getUniqueMessage(f));
                                }
                            }
                            valueSet.add(val);
                        }
                    }
                }
                //2、循环集合,验证每个唯一字段是否在数据库中有重复值
                for (T t : list) {
                    for (Field f : beanInfo.uniqueFields) {
                        Object val = f.get(t);
                        if (!isUnique(f.getName(), val, (String) beanInfo.pkField.get(t))) {
                            throw BaseRuntimeException.getException(getUniqueMessage(f));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    private String getUniqueMessage(Field field) {
        Unique anno = field.getAnnotation(Unique.class);
        return StringUtil.format(anno.msg(), field.getName());
    }

    /**
     * 字段唯一性验证
     *
     * @param fieldName  属性名称
     * @param val        属性值
     * @param excludeIds 排除id数组
     * @return
     */
    private boolean isUnique(String fieldName, Object val, String... excludeIds) {
        List<T> resultList = mongoTemplate.find(new Query(Criteria.where(fieldName).is(val)), beanInfo.clazz);
        if (resultList.isEmpty()) {
            return true;
        } else {
            if (excludeIds == null || excludeIds.length == 0) {
                return false;
            } else {
                Set<String> excludeIdSet = Arrays.stream(excludeIds).filter(Objects::nonNull).collect(Collectors.toSet());
                if (excludeIdSet.isEmpty()) {
                    return false;
                } else {
                    return resultList.stream().allMatch(e -> {
                        try {
                            return excludeIdSet.contains(beanInfo.pkField.get(e));
                        } catch (IllegalAccessException ex) {
                            throw BaseRuntimeException.getException(ex);
                        }
                    });
                }
            }
        }
    }
}
