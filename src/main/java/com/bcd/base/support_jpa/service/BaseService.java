package com.bcd.base.support_jpa.service;

import cn.hutool.core.text.StrFormatter;
import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.anno.Unique;
import com.bcd.base.support_jpa.bean.info.BeanInfo;
import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.base.support_jpa.util.ConditionUtil;
import com.bcd.base.util.StringUtil;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<T> findAllById(K ... kArr) {
        return repository.findAllById(Arrays.asList(kArr));
    }

    public T findById(K k) {
        return repository.findById(k).orElse(null);
    }


    public T findOne(Condition condition) {
        Specification<T> specification = ConditionUtil.toSpecification(condition);
        return repository.findOne(specification).orElse(null);
    }


    @Transactional
    public T save(T t) {
        return repository.save(t);
    }

    @Transactional
    public List<T> saveAll(Iterable<T> iterable) {
        return repository.saveAll(iterable);
    }


    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional
    public void deleteById(K id) {
        repository.deleteById(id);
    }

    @Transactional
    public void delete(T t) {
        repository.delete(t);
    }

    @Transactional
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    @Transactional
    public void deleteAllInBatch(Iterable<T> iterable) {
        repository.deleteAllInBatch(iterable);
    }

    @Transactional
    public void deleteAllInBatch(T ... tArr) {
        if(tArr.length==0){
            return;
        }
        repository.deleteAllInBatch(Arrays.asList(tArr));
    }

    @Transactional
    public void deleteAllByIdInBatch(Iterable<K> iterable) {
        repository.deleteAllByIdInBatch(iterable);
    }

    @Transactional
    public void deleteAllByIdInBatch(K ... ids) {
        if(ids.length==0){
            return;
        }
        repository.deleteAllByIdInBatch(Arrays.asList(ids));
    }


    /**
     * 优于普通删除方法
     * <p>
     * 注意:调用此方法的方法必须加上 @Transactional
     *
     * @param condition
     * @return 删除的记录条数
     */
    @Transactional
    public int delete(Condition condition) {
        Specification specification = ConditionUtil.toSpecification(condition);
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(getBeanInfo().clazz);
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(getBeanInfo().clazz);
        Predicate predicate = specification.toPredicate(criteriaDelete.from(getBeanInfo().clazz), criteriaQuery, criteriaBuilder);
        criteriaDelete.where(predicate);
        return em.createQuery(criteriaDelete).executeUpdate();
    }


    /**
     * 优于普通更新方法
     * <p>
     * 注意:调用此方法的方法必须加上 @Transactional
     *
     * @param condition
     * @param attrMap   更新的字段和值的map
     * @return 更新的记录条数
     */
    @Transactional
    public int update(Condition condition, Map<String, Object> attrMap) {
        if (attrMap == null || attrMap.size() == 0) {
            return 0;
        }
        Specification specification = ConditionUtil.toSpecification(condition);
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(getBeanInfo().clazz);
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(getBeanInfo().clazz);
        Predicate predicate = specification.toPredicate(criteriaUpdate.from(getBeanInfo().clazz), criteriaQuery, criteriaBuilder);
        criteriaUpdate.where(predicate);
        for (Map.Entry<String, Object> entry : attrMap.entrySet()) {
            criteriaUpdate.set(entry.getKey(), entry.getValue());
        }
        return em.createQuery(criteriaUpdate).executeUpdate();
    }

    /**
     * 执行native sql
     * query.getResultList() 结果类型为 List<Map>
     *
     * @param sql
     * @return
     */
    @Transactional
    public Query executeNativeSql(String sql, Object... params) {
        Query query = em.createNativeQuery(sql);
        //设置返回的结果集为List<Map>形式;如果不设置,则默认为List<Object[]>
        query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        if (params != null) {
            for (int i = 0; i <= params.length - 1; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        return query;
    }



    /**
     * 字段唯一性验证
     * <p>
     * 通过{@link Id}识别主键,不支持联合主键
     *
     * @param fieldName  属性名称
     * @param val        属性值
     * @param excludeIds 排除id数组
     * @return
     */
    public boolean isUnique(String fieldName, Object val, K... excludeIds) {
        List<T> resultList = repository.findAll((Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
            {
                Predicate predicate = criteriaBuilder.conjunction();
                List<Expression<Boolean>> expressions = predicate.getExpressions();
                expressions.add(criteriaBuilder.equal(root.get(fieldName), val));
                return predicate;
            }
        });
        if (resultList.isEmpty()) {
            return true;
        } else {
            if (excludeIds == null || excludeIds.length == 0) {
                return false;
            } else {
                Set<K> excludeIdSet = Arrays.stream(excludeIds).filter(Objects::nonNull).collect(Collectors.toSet());
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


    /**
     * 获取唯一注解字段的message值
     *
     * @param field
     * @return
     */
    private String getUniqueMessage(Field field) {
        Unique anno = field.getAnnotation(Unique.class);
        return StringUtil.format(anno.value(),field.getName());
    }

    /**
     * 保存前进行唯一性验证
     *
     * @param t
     */
    public void validateUniqueAnno(T t) {
        if (!getBeanInfo().isCheckUnique) {
            return;
        }
        //1、循环集合,验证每个唯一字段是否在数据库中有重复值
        for (Field f : getBeanInfo().uniqueFieldList) {
            try {
                Object val = f.get(t);
                if (!isUnique(f.getName(), val, (K) getBeanInfo().pkField.get(t))) {
                    throw BaseRuntimeException.getException(getUniqueMessage(f));
                }
            } catch (IllegalAccessException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 保存前进行批量唯一性验证
     *
     * @param iterable
     */
    public void validateUniqueAnno(Iterable<T> iterable) {
        if (!getBeanInfo().isCheckUnique) {
            return;
        }
        try {
            //1、循环集合,看传入的参数集合中唯一字段是否有重复的值
            Map<String, Set<Object>> fieldValueSetMap = new HashMap<>();
            for (T t : iterable) {
                for (Field f : getBeanInfo().uniqueFieldList) {
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
            //2、循环集合,验证每个唯一字段是否在数据库中有重复值
            for (T t : iterable) {
                for (Field f : getBeanInfo().uniqueFieldList) {
                    Object val = f.get(t);
                    if (!isUnique(f.getName(), val, (K) getBeanInfo().pkField.get(t))) {
                        throw BaseRuntimeException.getException(getUniqueMessage(f));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
