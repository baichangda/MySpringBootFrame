package com.bcd.base.support_mongodb.service;

import com.google.common.collect.Streams;
import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.util.ConditionUtil;
import com.bcd.base.util.StringUtil;
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

/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class BaseService<T> {

    /**
     * 注意所有的类变量必须使用get方法获取
     * 因为类如果被aop代理了、代理对象的这些变量值都是null
     * 而get方法会被委托给真实对象的方法
     */

    @Autowired(required = false)
    public BaseRepository<T> repository;
    @Autowired
    public MongoTemplate mongoTemplate;
    private final BeanInfo<T> beanInfo;


    public BaseRepository<T> getRepository() {
        return repository;
    }

    public BeanInfo<T> getBeanInfo() {
        return beanInfo;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public BaseService() {
        final Class<T> beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo<>(beanClass);
    }

    public final List<T> list() {
        return getRepository().findAll();
    }

    public final List<T> list(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().find(query, getBeanInfo().clazz);
    }

    public final List<T> list(Sort sort) {
        return getRepository().findAll(sort);
    }

    public final List<T> list(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return getMongoTemplate().find(query, getBeanInfo().clazz);
    }

    public final Page<T> page(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public final Page<T> page(Condition condition, Pageable pageable) {
        Query query = ConditionUtil.toQuery(condition);
        final long total = getMongoTemplate().count(query, getBeanInfo().clazz);
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (total > offset) {
            query.with(pageable);
            List<T> list = getMongoTemplate().find(query, getBeanInfo().clazz);
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, total);
        }
    }

    public final long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().count(query, getBeanInfo().clazz);
    }

    public final T get(String id) {
        return getRepository().findById(id).orElse(null);
    }

    public final T get(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().findOne(query, (Class<T>) getBeanInfo().clazz);
    }

    public final T save(T t) {
        validateUniqueBeforeSave(Collections.singletonList(t));
        return getRepository().save(t);
    }

    public final List<T> save(Iterable<T> iterable) {
        validateUniqueBeforeSave(Streams.stream(iterable).toList());
        return getRepository().saveAll(iterable);
    }

    /**
     * 删除所有数据
     */
    public final void delete() {
        getRepository().deleteAll();
    }

    /**
     * 根据id删除
     *
     * @param ids
     */
    public final void delete(String... ids) {
        if (ids.length == 1) {
            getRepository().deleteById(ids[0]);
        } else if (ids.length > 1) {
            Object[] newIds = new Object[ids.length];
            System.arraycopy(ids, 0, newIds, 0, ids.length);
            Query query = new Query(Criteria.where(getBeanInfo().pkFieldName).in(newIds));
            getMongoTemplate().remove(query, getBeanInfo().clazz);
        }
    }

    /**
     * 根据条件删除
     *
     * @param condition
     */
    public final void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        getMongoTemplate().remove(query, getBeanInfo().clazz);
    }


    private void validateUniqueBeforeSave(List<T> list) {
        if (getBeanInfo().uniqueFields.length > 0) {
            try {
                //1、循环集合,看传入的参数集合中唯一字段是否有重复的值
                if (list.size() > 1) {
                    Map<String, Set<Object>> fieldValueSetMap = new HashMap<>();
                    for (T t : list) {
                        for (Field f : getBeanInfo().uniqueFields) {
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
                    for (Field f : getBeanInfo().uniqueFields) {
                        Object val = f.get(t);
                        if (!isUnique(f.getName(), val, (String) getBeanInfo().pkField.get(t))) {
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
        List<T> resultList = getMongoTemplate().find(new Query(Criteria.where(fieldName).is(val)), getBeanInfo().clazz);
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
                            return excludeIdSet.contains(getBeanInfo().pkField.get(e));
                        } catch (IllegalAccessException ex) {
                            throw BaseRuntimeException.getException(ex);
                        }
                    });
                }
            }
        }
    }
}
