package com.bcd.rdb.service;

import com.bcd.base.i18n.I18NData;
import com.bcd.rdb.anno.Unique;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.condition.Condition;
import com.bcd.rdb.bean.info.BeanInfo;
import com.bcd.rdb.jdbc.rowmapper.MyColumnMapRowMapper;
import com.bcd.rdb.util.ConditionUtil;
import com.bcd.rdb.repository.BaseRepository;
import org.apache.commons.lang3.ClassUtils;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.*;
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

    @Autowired
    public BaseRepository<T, K> repository;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    private volatile BeanInfo beanInfo;


    /**
     * 获取当前service对应实体类的信息
     *
     * @return
     */
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

    public List<T> findAllById(K[] kArr) {
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
        validateUniqueBeforeSave(t);
        return repository.save(t);
    }

    @Transactional
    public List<T> saveAll(Iterable<T> iterable) {
        validateUniqueBeforeSave(iterable);
        return repository.saveAll(iterable);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional
    public void deleteById(K... ids) {
        for (K id : ids) {
            repository.deleteById(id);
        }
    }

    @Transactional
    public void delete(T t) {
        repository.delete(t);
    }

    @Transactional
    public void deleteAll(Iterable<T> iterable) {
        repository.deleteAll(iterable);
    }

    @Transactional
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
    }

    @Transactional
    public void deleteInBatch(Iterable<T> iterable) {
        repository.deleteInBatch(iterable);
    }


    /**
     * 优于普通删除方法
     *
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
     *
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
        Iterator<Map.Entry<String, Object>> it = attrMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
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
     * 分页查询
     *
     * @param countSql 统计数量sql
     * @param sql      查询结果集sql(不带limit)
     * @param pageable 分页对象参数
     * @param params   参数(用于countSql和sql)
     * @return
     */
    public Page<Map<String, Object>> queryByNativeSql(String countSql, String sql, Pageable pageable, Object... params) {
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        if (count == null || count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } else {
            String limitSql = sql + "\n limit ?,?";
            Object[] limitParams;
            if (params == null || params.length == 0) {
                limitParams = new Object[2];
            } else {
                limitParams = Arrays.copyOf(params, params.length + 2);
            }
            limitParams[limitParams.length - 2] = pageable.getPageNumber() * pageable.getPageSize();
            limitParams[limitParams.length - 1] = pageable.getPageSize();
            List<Map<String, Object>> dataList = jdbcTemplate.query(limitSql, MyColumnMapRowMapper.ROW_MAPPER, limitParams);
            return new PageImpl<>(dataList, pageable, count);
        }
    }

    /**
     * 分页查询并转换为实体类
     *
     * @param sql
     * @param pageable
     * @param clazz
     * @param params
     * @return
     */
    public Page<T> queryByNativeSql(String countSql, String sql, Pageable pageable, Class<T> clazz, Object... params) {
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        if (count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } else {
            String limitSql = sql + "\n limit ?,?";
            Object[] limitParams;
            if (params == null || params.length == 0) {
                limitParams = new Object[2];
            } else {
                limitParams = Arrays.copyOf(params, params.length + 2);
            }
            limitParams[limitParams.length - 2] = pageable.getPageNumber() * pageable.getPageSize();
            limitParams[limitParams.length - 1] = pageable.getPageSize();
            List<T> dataList = jdbcTemplate.query(limitSql, new BeanPropertyRowMapper<>(clazz), limitParams);
            return new PageImpl<>(dataList, pageable, count);
        }
    }

    /**
     * 采用jdbc查询方式,将condition转换为where条件
     * condition中的字段名称将会由驼峰格式转换为下划线拼接格式
     *
     * @param sqlPre    sql语句前缀,where之前
     *                  example
     *                  select * from t
     *                  select a.name,b.* from a inner join b on a.id=b.relation_id
     * @param sqlSuffix sql语句结尾,where之后
     *                  example:
     *                  limit 1,10
     *                  order by id desc
     * @param condition 条件
     * @param clazz     返回结果集
     *                  三种情况
     *                  1、java 8大基础类型和包装类型,String
     *                  2、自定义对象类型
     * @param <R>       结果集类型
     * @return
     */
    public <R> List<R> queryByCondition(String sqlPre, Condition condition, String sqlSuffix, Class<R> clazz) {
        Map<String, Object> paramMap = new HashMap<>();
        String where = ConditionUtil.convertCondition(condition, paramMap);
        StringBuilder sql = new StringBuilder(sqlPre);
        if (where != null) {
            sql.append(" where");
            sql.append("\n");
            sql.append(where);
        }
        if (sqlSuffix != null) {
            sql.append("\n");
            sql.append(sqlSuffix);
        }
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        if (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class) {
            return namedParameterJdbcTemplate.queryForList(sql.toString(), paramMap, clazz);
        }else{
            return namedParameterJdbcTemplate.query(sql.toString(), paramMap, new BeanPropertyRowMapper<>(clazz));
        }
    }

    /**
     * 逻辑和参数定义参考
     * {@link BaseService#queryByCondition(String, Condition, String, Class)}
     *
     * 返回的结果类型为map
     * @param sqlPre
     * @param condition
     * @param sqlSuffix
     * @return
     */
    public List<Map<String,Object>> queryByCondition(String sqlPre, Condition condition, String sqlSuffix){
        Map<String, Object> paramMap = new HashMap<>();
        String where = ConditionUtil.convertCondition(condition, paramMap);
        StringBuilder sql = new StringBuilder(sqlPre);
        if (where != null) {
            sql.append(" where");
            sql.append("\n");
            sql.append(where);
        }
        if (sqlSuffix != null) {
            sql.append("\n");
            sql.append(sqlSuffix);
        }
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedParameterJdbcTemplate.query(sql.toString(),paramMap, MyColumnMapRowMapper.ROW_MAPPER);
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
                if(excludeIdSet.isEmpty()){
                    return false;
                }else {
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
        String msg = anno.messageValue();
        if (StringUtils.isEmpty(msg)) {
            msg = I18NData.getI18NData(anno.messageKey()).getValue(field.getName());
        }
        return msg;
    }

    /**
     * 保存前进行唯一性验证
     *
     * @param t
     */
    public void validateUniqueBeforeSave(T t) {
        if (!getBeanInfo().isCheckUnique) {
            return;
        }
        //1、循环集合,验证每个唯一字段是否在数据库中有重复值
        for (Field f : getBeanInfo().uniqueFieldList) {
            try {
                Object val = f.get(t);
                if (!isUnique(f.getName(), val, (K)getBeanInfo().pkField.get(t))) {
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
    public void validateUniqueBeforeSave(Iterable<T> iterable) {
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
