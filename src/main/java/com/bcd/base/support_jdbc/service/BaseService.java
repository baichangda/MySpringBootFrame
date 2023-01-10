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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BaseService<T extends BaseBean> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public final BeanInfo<T> beanInfo;

    public BaseService() {
        final Class<T> beanClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        beanInfo = new BeanInfo<>(beanClass);
    }

    /**
     * 查询一条数据、如果有多条则取第一条、如果没有数据返回null
     *
     * @param condition
     * @return
     */
    public final T get(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final List<T> list = list(convertRes, null, -1, -1);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public final List<T> list() {
        return list(null, null);
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public final List<T> list(Condition condition) {
        return list(condition, null);
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public final List<T> list(Sort sort) {
        return list(null, sort);
    }

    /**
     * 根据条件查询并排序
     *
     * @param condition 条件、可以为null
     * @param sort      排序、可以为null
     * @return
     */
    public final List<T> list(Condition condition, Sort sort) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        return list(convertRes, sort, -1, -1);
    }

    /**
     * 分页查询
     *
     * @return
     */
    public final Page<T> page(Pageable pageable) {
        return page(null, pageable);
    }

    /**
     * 分页查询
     *
     * @param condition
     * @param pageable
     * @return
     */
    public final Page<T> page(Condition condition, Pageable pageable) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final int total = count(convertRes);
        final int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (total > offset) {
            final List<T> content = list(convertRes, pageable.getSort(), offset, pageable.getPageSize());
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
    public final T get(long id) {
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
    public final void save(T t) {
        final Long id = t.id;
        if (id == null) {
            insert(t);
        } else {
            update(t);
        }
    }

    /**
     * 新增
     * 会更新对象、设置id
     *
     * @param t
     */
    public final void insert(T t) {
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
     * 根据id更新
     *
     * @param t
     */
    public final void update(T t) {
        final String sql = beanInfo.updateSql + " where id=?";
        final List<Object> args = beanInfo.getValues(t);
        args.add(t.id);
        jdbcTemplate.update(sql, args.toArray());
    }

    /**
     * 通过condition更新
     *
     * @param condition 更新条件
     * @param vals      更新的字段名称和值、奇数位置必须为字段名、偶数位置必须为值
     */
    public final void update(Condition condition, Object... vals) {
        final StringBuilder sql = new StringBuilder("update ");
        sql.append(beanInfo.tableName);
        sql.append(" set ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < vals.length; i += 2) {
            final String name = (String) vals[i];
            final Object val = vals[i + 1];
            if (i != 0) {
                sql.append(",");
            }
            sql.append(beanInfo.toColumnName(name));
            sql.append("=?");
            args.add(val);
        }
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            args.addAll(convertRes.paramList);
        }
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    /**
     * 批量新增
     * 不会改变对象
     *
     * @param list
     */
    public final void insertBatch(List<T> list) {
        final List<Object[]> argList = list.stream().map(e1 -> beanInfo.getValues(e1).toArray()).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(beanInfo.insertSql, argList);
    }

    /**
     * 批量更新
     *
     * @param list
     */
    public final void updateBatch(List<T> list) {
        String sql = beanInfo.updateSql + " where id=?";

        final List<Object[]> argList = list.stream().map(e1 -> {
            final List<Object> args = beanInfo.getValues(e1);
            args.add(e1.id);
            return args.toArray();
        }).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, argList);
    }

    /**
     * 根据id批量删除、使用批量删除
     *
     * @param ids
     */
    public final void delete(long... ids) {
        if (ids.length == 1) {
            final String sql = "delete from " + beanInfo.tableName + " where id =?";
            jdbcTemplate.update(sql, ids[0]);
        } else if (ids.length > 1) {
            final List<Object[]> argList = Arrays.stream(ids).mapToObj(e -> new Object[]{e}).collect(Collectors.toList());
            final String sql = "delete from " + beanInfo.tableName + " where id =?";
            jdbcTemplate.batchUpdate(sql, argList);
        }
    }

    /**
     * 删除所有数据
     */
    public final void delete() {
        delete((Condition) null);
    }

    /**
     * 根据条件删除
     */
    public final void delete(Condition condition) {
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

    /**
     * 统计所有数量
     *
     * @return
     */
    public final int count() {
        return count((ConvertRes) null);
    }

    /**
     * 统计数量
     *
     * @param condition
     * @return
     */
    public final int count(Condition condition) {
        return count(ConditionUtil.convertCondition(condition, beanInfo));
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

    private List<T> list(ConvertRes convertRes, Sort sort, int offset, int limit) {
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