package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.bean.BaseBean;
import com.bcd.base.support_mongodb.bean.SuperBaseBean;
import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.util.ConditionUtil;
import com.bcd.base.util.StringUtil;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.data.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/8/25.
 */
@SuppressWarnings("unchecked")
public class BaseService<T extends SuperBaseBean> {

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

    /**
     * 获取代理对象
     * 需要如下注解开启 @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
     * 如下场景使用
     * 同一个service中a()调用b()、其中b()符合aop切面定义、此时不会走aop逻辑、因为此时执行a()中this对象已经不是代理对象、此时需要getProxy().b()
     * 注意:
     * 此方法不要乱用、避免造成性能损失
     */
    protected BaseService<T> getProxy() {
        return (BaseService<T>) AopContext.currentProxy();
    }

    public List<T> list() {
        return getRepository().findAll();
    }

    public List<T> list(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().find(query, getBeanInfo().clazz);
    }

    public List<T> list(Sort sort) {
        return getRepository().findAll(sort);
    }

    public List<T> list(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        query.with(sort);
        return getMongoTemplate().find(query, getBeanInfo().clazz);
    }

    public Page<T> page(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public Page<T> page(Condition condition, Pageable pageable) {
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

    public long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().count(query, getBeanInfo().clazz);
    }

    public T get(String id) {
        return getRepository().findById(id).orElse(null);
    }

    public T get(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().findOne(query, getBeanInfo().clazz);
    }

    public T save(T t) {
        validateUniqueBeforeSave(Collections.singletonList(t));
        if (getBeanInfo().isBaseBean) {
            if (t.getId() == null) {
                setCreateInfo(t);
            } else {
                setUpdateInfo(t);
            }
        }
        return getRepository().save(t);
    }

    public List<T> save(List<T> collection) {
        validateUniqueBeforeSave(collection);
        if (getBeanInfo().isBaseBean) {
            for (T t : collection) {
                if (t.getId() == null) {
                    setCreateInfo(t);
                } else {
                    setUpdateInfo(t);
                }
            }
        }
        return getRepository().saveAll(collection);
    }

    public List<T> insertAll(List<T> collection) {
        validateUniqueBeforeSave(collection);
        if (getBeanInfo().isBaseBean) {
            for (T t : collection) {
                setCreateInfo(t);
            }
        }
        return getMongoTemplate().insertAll(collection).stream().toList();
    }

    /**
     * 删除所有数据
     */
    public void delete() {
        getRepository().deleteAll();
    }

    /**
     * 根据id删除
     *
     * @param ids
     */
    public void delete(String... ids) {
        if (ids.length == 1) {
            getRepository().deleteById(ids[0]);
        } else if (ids.length > 1) {
            Object[] newIds = new Object[ids.length];
            System.arraycopy(ids, 0, newIds, 0, ids.length);
            Query query = new Query(Criteria.where("id").in(newIds));
            getMongoTemplate().remove(query, getBeanInfo().clazz);
        }
    }

    /**
     * 根据条件删除
     *
     * @param condition
     */
    public void delete(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        getMongoTemplate().remove(query, getBeanInfo().clazz);
    }


    /**
     * 批量修改、不会修改更新时间
     * @param condition
     * @param updates
     * @return
     */
    public BulkWriteResult updateMulti(Condition condition, Update... updates) {
        if (updates.length == 0) {
            return null;
        } else {
            Query query = ConditionUtil.toQuery(condition);
            List<Pair<Query, UpdateDefinition>> collect = Arrays.stream(updates).map(e -> Pair.of(query, (UpdateDefinition) e)).toList();
            return getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, getBeanInfo().clazz).updateMulti(collect).execute();
        }
    }


    private void setCreateInfo(T t) {
        BaseBean bean = (BaseBean) t;
        bean.createTime = new Date();
        //todo 在这里获取本地用户设置创建信息
//        UserBean user = SaTokenUtil.getLoginUser_cache();
//        if (user != null) {
//            bean.createUserId = user.getId();
//            bean.createUserName = user.getUsername();
//        }
    }

    private void setUpdateInfo(T t) {
        BaseBean bean = (BaseBean) t;
        bean.createTime = new Date();
        //todo 在这里获取本地用户设置更新信息
//        UserBean user = SaTokenUtil.getLoginUser_cache();
//        if (user != null) {
//            bean.updateUserId = user.getId();
//            bean.updateUserName = user.getUsername();
//        }
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
                        if (!isUnique(f.getName(), val, t.getId())) {
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
                    return resultList.stream().allMatch(e -> excludeIdSet.contains(e.getId()));
                }
            }
        }
    }
}
