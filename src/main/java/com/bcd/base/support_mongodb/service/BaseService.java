package com.bcd.base.support_mongodb.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseException;
import com.bcd.base.support_mongodb.anno.Unique;
import com.bcd.base.support_mongodb.bean.BaseBean;
import com.bcd.base.support_mongodb.bean.SuperBaseBean;
import com.bcd.base.support_mongodb.bean.UserInterface;
import com.bcd.base.support_mongodb.util.ConditionUtil;
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


    public MongoTemplate mongoTemplate;

    private final BeanInfo<T> beanInfo;


    public BeanInfo<T> getBeanInfo() {
        return beanInfo;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Autowired
    public void init(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
        return list(null, null);
    }

    public List<T> list(Condition condition) {
        return list(condition, null);
    }

    public List<T> list(Sort sort) {
        return list(null, sort);
    }

    public List<T> list(Condition condition, Sort sort) {
        Query query = ConditionUtil.toQuery(condition);
        if (sort != null) {
            query.with(sort);
        }
        return getMongoTemplate().find(query, getBeanInfo().clazz);
    }

    public Page<T> page(Pageable pageable) {
        return page(null, pageable);
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
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }
    }

    public long count(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().count(query, getBeanInfo().clazz);
    }

    public T get(String id) {
        return getMongoTemplate().findById(id, getBeanInfo().clazz);
    }

    public T get(Condition condition) {
        Query query = ConditionUtil.toQuery(condition);
        return getMongoTemplate().findOne(query, getBeanInfo().clazz);
    }

    /**
     * 会验证{@link Unique}
     * 会设置创建信息或更新信息
     *
     * @param t
     * @return
     */
    public T save(T t) {
        validateUniqueBeforeSave(Collections.singletonList(t));
        if (t.getId() == null) {
            if (getBeanInfo().autoSetCreateInfo) {
                setCreateInfo(t);
            }
        } else {
            if (getBeanInfo().autoSetUpdateInfo) {
                setUpdateInfo(t);
            }
        }
        return getMongoTemplate().save(t);
    }

    /**
     * 会验证{@link Unique}
     * 会设置创建信息
     *
     * @param collection
     * @return
     */
    public List<T> insertAll(List<T> collection) {
        validateUniqueBeforeSave(collection);
        if (getBeanInfo().autoSetCreateInfo) {
            for (T t : collection) {
                setCreateInfo(t);
            }
        }
        return getMongoTemplate().insertAll(collection).stream().toList();
    }

    /**
     * 删除所有数据
     */
    public void deleteAll() {
        getMongoTemplate().remove(getBeanInfo().clazz);
    }

    /**
     * 根据id删除
     *
     * @param ids
     */
    public void delete(String... ids) {
        if (ids.length == 1) {
            getMongoTemplate().remove(new Query(Criteria.where("id").is(ids[0])));
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
     * 批量修改
     * 不会修改更新时间
     *
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
        UserInterface user = getLoginUser();
        if (user != null) {
            bean.createUserId = user.getId();
            bean.createUserName = user.getUsername();
        }
    }

    private void setUpdateInfo(T t) {
        BaseBean bean = (BaseBean) t;
        bean.createTime = new Date();
        UserInterface user = getLoginUser();
        if (user != null) {
            bean.updateUserId = user.getId();
            bean.updateUserName = user.getUsername();
        }
    }

    private void validateUniqueBeforeSave(List<T> list) {
        if (getBeanInfo().uniqueInfos.length > 0) {
            try {
                //1、循环集合,看传入的参数集合中唯一字段是否有重复的值
                if (list.size() > 1) {
                    Map<String, Set<Object>> fieldValueSetMap = new HashMap<>();
                    for (T t : list) {
                        for (UniqueInfo uniqueInfo : getBeanInfo().uniqueInfos) {
                            Field field = uniqueInfo.field;
                            String fieldName = uniqueInfo.fieldName;
                            Object val = field.get(t);
                            Set<Object> valueSet = fieldValueSetMap.get(fieldName);
                            if (valueSet == null) {
                                valueSet = new HashSet<>();
                                fieldValueSetMap.put(fieldName, valueSet);
                            } else {
                                if (valueSet.contains(val)) {
                                    throw BaseException.get(uniqueInfo.msg).code(uniqueInfo.code);
                                }
                            }
                            valueSet.add(val);
                        }
                    }
                }
                //2、循环集合,验证每个唯一字段是否在数据库中有重复值
                for (T t : list) {
                    for (UniqueInfo uniqueInfo : getBeanInfo().uniqueInfos) {
                        Object val = uniqueInfo.field.get(t);
                        if (!isUnique(uniqueInfo.fieldName, val, t.getId())) {
                            throw BaseException.get(uniqueInfo.msg).code(uniqueInfo.code);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw BaseException.get(e);
            }
        }
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

    /**
     * 此方法主要是给内部创建信息、更新信息获取当前登陆用户使用
     * 不允许调用
     * 其内容在代码创建之初就已经确定下来
     * 如果没有用户体系、则实现返回null即可
     *
     * @return
     */
    private static UserInterface getLoginUser() {
        return null;
    }
}
