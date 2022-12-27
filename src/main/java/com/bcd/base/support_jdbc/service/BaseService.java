package com.bcd.base.support_jdbc.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_jdbc.bean.BaseBean;
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
        final Class beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo(beanClass);
    }

    /**
     * 查询一条数据、如果有多条则取第一条、如果没有数据返回null
     *
     * @param condition
     * @return
     */
    public T findOne(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final List<T> list = findAll(convertRes, null, -1, -1);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 查询表中所有数据
     *
     * @return
     */
    public List<T> findAll() {
        final String sql = "select * from " + beanInfo.tableName;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(beanInfo.clazz));
    }

    /**
     * 根据条件查询
     *
     * @param condition
     * @return
     */
    public List<T> findAll(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        return findAll(convertRes, null, -1, -1);
    }

    /**
     * 根据条件查询并排序
     *
     * @param condition
     * @param sort
     * @return
     */
    public List<T> findAll(Condition condition, Sort sort) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        return findAll(convertRes, sort, -1, -1);
    }

    /**
     * 分页查询
     *
     * @param condition
     * @param pageable
     * @return
     */
    public Page<T> findAll(Condition condition, Pageable pageable) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final int total = count(convertRes);
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (total > offset) {
            final List<T> content = findAll(convertRes, pageable.getSort(), offset, pageable.getPageSize());
            return new PageImpl<>(content, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, total);
        }
    }

    /**
     * 根据id查找对象
     *
     * @param id
     * @return
     */
    public T findById(long id) {
        final String sql = "select * from " + beanInfo.tableName + " where id=?";
        final List<T> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(beanInfo.clazz), id);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 保存
     * 根据id判断是否新增或者更新
     * 如果是新增、会更新对象、设置id
     *
     * @param t
     */
    public void save(T t) {
        final Long id = t.id;
        if (id == null) {
            insert(t);
        } else {
            update(t);
        }
    }

    /**
     * 批量保存
     *
     * @param list
     * @return
     */
    public List<T> saveAll(List<T> list) {
        for (T t : list) {
            save(t);
        }
        return list;
    }

    /**
     * 新增
     * 会更新对象、设置id
     *
     * @param t
     */
    public void insert(T t) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            final PreparedStatement ps = conn.prepareStatement(beanInfo.insertSql, Statement.RETURN_GENERATED_KEYS);
            final ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(beanInfo.getValues(t).toArray());
            argumentPreparedStatementSetter.setValues(ps);
            return ps;
        }, keyHolder);
        t.id = keyHolder.getKey().longValue();
    }

    /**
     * 更新
     *
     * @param t
     */
    public void update(T t) {
        final String sql = beanInfo.updateSql + " where id=?";
        final List<Object> args = beanInfo.getValues(t);
        args.add(t.id);
        jdbcTemplate.update(sql, args.toArray());
    }

    /**
     * 批量新增
     * 不会改变对象
     *
     * @param list
     */
    public void insertBatch(List<T> list) {
        final List<Object[]> argList = list.stream().map(e1 -> beanInfo.getValues(e1).toArray()).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(beanInfo.insertSql, argList);
    }

    /**
     * 批量更新
     *
     * @param list
     */
    public void updateBatch(List<T> list) {
        final StringBuilder sql = new StringBuilder();
        sql.append(beanInfo.updateSql);
        sql.append(" where id=?");

        final List<Object[]> argList = list.stream().map(e1 -> {
            final List<Object> args = beanInfo.getValues(e1);
            args.add(e1.id);
            return args.toArray();
        }).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql.toString(), argList);
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    public void deleteById(long id) {
        final String sql = "delete from " + beanInfo.tableName + " where id=?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * 根据id批量删除、使用in语法
     *
     * @param ids
     */
    public void deleteByIds(Long... ids) {
        final StringJoiner sj = new StringJoiner(",");
        final Object[] params = new Object[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sj.add("?");
            params[i] = ids[i];
        }
        final String sql = "delete from " + beanInfo.tableName + " where id in(" + sj + ")";
        jdbcTemplate.update(sql, params);
    }

    /**
     * 删除所有数据
     */
    public void deleteAll() {
        final String sql = "delete from " + beanInfo.tableName;
        jdbcTemplate.update(sql);
    }

    /**
     * 根据条件删除
     */
    public void deleteAll(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(beanInfo.tableName);
        final List<Object> paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = null;
        }
        if (paramList != null && !paramList.isEmpty()) {
            jdbcTemplate.update(sql.toString(), paramList.toArray());
        } else {
            jdbcTemplate.update(sql.toString());
        }
    }

    private int count(ConvertRes convertRes) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(beanInfo.tableName);
        final List<Object> paramList;
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
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        }
    }

    private List<T> findAll(ConvertRes convertRes, Sort sort, int offset, int limit) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(beanInfo.tableName);
        final List<Object> paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = new ArrayList<>();
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