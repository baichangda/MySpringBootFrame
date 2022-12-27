package com.bcd.base.support_jdbc.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.bean.BaseBean;
import com.bcd.base.support_jdbc.condition.BeanInfo;
import com.bcd.base.support_jdbc.condition.ConditionUtil;
import com.bcd.base.support_jdbc.condition.ConvertRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BaseService<T extends BaseBean> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public final BeanInfo<T> beanInfo;

    public BaseService() {
        Class beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo(beanClass);
    }

    public T findOne(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition);
        final List<T> list = findAll(convertRes, null, -1, -1);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public List<T> findAll() {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(beanInfo.tableName);
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(beanInfo.clazz));
    }

    public List<T> findAll(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition);
        return findAll(convertRes, null, -1, -1);
    }

    public List<T> findAll(Condition condition, Sort sort) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition);
        return findAll(convertRes, sort, -1, -1);
    }

    public Page<T> findAll(Condition condition, Pageable pageable) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition);
        final int total = count(convertRes);
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (total > offset) {
            final List<T> content = findAll(convertRes, pageable.getSort(), offset, pageable.getPageNumber());
            return new PageImpl<>(content, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>());
        }
    }

    public T findById(long id) {
        String sql = "select * from " + beanInfo.tableName + " where id=?";
        final List<T> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(beanInfo.clazz), id);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public void save(T t) {
        final Long id = t.id;
        if (id == null) {
            insert(t);
        } else {
            update(t);
        }
    }

    public List<T> saveAll(List<T> list) {
        for (T t : list) {
            save(t);
        }
        return list;
    }

    public void deleteById(long id) {
        String sql = "delete from " + beanInfo.tableName + " where id=?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteByIds(Long... ids) {
        StringJoiner sj = new StringJoiner(",");
        Object[] params = new Object[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sj.add("?");
            params[i] = ids[i];
        }
        String sql = "delete from " + beanInfo.tableName + " where id in(" + sj + ")";
        jdbcTemplate.update(sql, params);
    }

    public void deleteAll() {
        String sql = "delete from " + beanInfo.tableName;
        jdbcTemplate.update(sql);
    }


    public void insert(T t) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            final PreparedStatement ps = conn.prepareStatement(beanInfo.insertSql, Statement.RETURN_GENERATED_KEYS);
            final ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(beanInfo.fieldList.stream().map(e -> {
                try {
                    return e.get(t);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            }).toArray());
            argumentPreparedStatementSetter.setValues(ps);
            return ps;
        }, keyHolder);
        t.id = keyHolder.getKey().longValue();
    }

    public void insertBatch(List<T> list) {
        final List<Object[]> argList = list.stream().map(e1 ->
                beanInfo.fieldList.stream().map(e2 -> {
                    try {
                        return e2.get(e1);
                    } catch (IllegalAccessException ex) {
                        throw BaseRuntimeException.getException(ex);
                    }
                }).toArray()
        ).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(beanInfo.insertSql, argList);
    }

    public void update(T t) {
        StringBuilder sql = new StringBuilder();
        sql.append(beanInfo.updateSql);
        sql.append(" where id=?");
        final List<Object> args = beanInfo.fieldList.stream().map(e -> {
            try {
                return e.get(t);
            } catch (IllegalAccessException ex) {
                throw BaseRuntimeException.getException(ex);
            }
        }).collect(Collectors.toList());
        args.add(t.id);
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    public void updateBatch(List<T> list) {
        StringBuilder sql = new StringBuilder();
        sql.append(beanInfo.updateSql);
        sql.append(" where id=?");

        final List<Object[]> argList = list.stream().map(e1 -> {
            final List<Object> args = beanInfo.fieldList.stream().map(e2 -> {
                try {
                    return e2.get(e1);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            }).collect(Collectors.toList());
            args.add(e1.id);
            return args.toArray();
        }).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql.toString(), argList);
    }

    private int count(ConvertRes convertRes) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(beanInfo.tableName);
        final List paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = null;
        }

        if (paramList != null && !paramList.isEmpty()) {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, paramList.toArray());
        } else {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, new BeanPropertyRowMapper<>(beanInfo.clazz));
        }
    }

    private List<T> findAll(ConvertRes convertRes, Sort sort, int offset, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(beanInfo.tableName);
        final List paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = new ArrayList();
        }

        if (sort != null && sort.isSorted()) {
            sql.append(" order by ");
            final String orderBy = sort.stream().map(e -> e.getProperty() + " " + e.getDirection()).reduce((e1, e2) -> e1 + "," + e2).get();
            sql.append(orderBy);
        }

        if (offset != -1) {
            sql.append(" limit ?,?");
            paramList.add(offset);
            paramList.add(limit);
        }

        if (!paramList.isEmpty()) {
            return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(beanInfo.clazz), paramList.toArray());
        } else {
            return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(beanInfo.clazz));
        }
    }


}