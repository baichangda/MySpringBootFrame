package com.bcd.base.support_jdbc.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_jdbc.bean.BaseBean;
import com.bcd.base.support_jdbc.bean.SuperBaseBean;
import com.bcd.base.support_jdbc.condition.ConditionUtil;
import com.bcd.base.support_jdbc.condition.ConvertRes;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BaseService<T extends SuperBaseBean> {

    /**
     * 注意所有的类变量必须使用get方法获取
     * 因为类如果被aop代理了、代理对象的这些变量值都是null
     * 而get方法会被委托给真实对象的方法
     */

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    private final BeanInfo<T> beanInfo;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public BeanInfo<T> getBeanInfo() {
        return beanInfo;
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

    /**
     * 查询一条数据、如果有多条则取第一条、如果没有数据返回null
     *
     * @param condition
     * @return
     */
    public T get(Condition condition) {
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
    public List<T> list() {
        return list(null, null);
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<T> list(Condition condition) {
        return list(condition, null);
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<T> list(Sort sort) {
        return list(null, sort);
    }

    /**
     * 根据条件查询并排序
     *
     * @param condition 条件、可以为null
     * @param sort      排序、可以为null
     * @return
     */
    public List<T> list(Condition condition, Sort sort) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        return list(convertRes, sort, -1, -1);
    }

    /**
     * 分页查询
     *
     * @return
     */
    public Page<T> page(Pageable pageable) {
        return page(null, pageable);
    }

    /**
     * 分页查询
     *
     * @param condition
     * @param pageable
     * @return
     */
    public Page<T> page(Condition condition, Pageable pageable) {
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
    public T get(long id) {
        final String sql = "select * from " + getBeanInfo().tableName + " where id=?";
        final List<T> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<>(getBeanInfo().clazz), id);
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
     * 新增
     * 会更新对象、设置id
     *
     * @param t
     */
    public void insert(T t) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(conn -> {
            final PreparedStatement ps = conn.prepareStatement(getBeanInfo().insertSql, Statement.RETURN_GENERATED_KEYS);
            final ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(getBeanInfo().getValues(t).toArray());
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
    public void update(T t) {
        final String sql = getBeanInfo().updateSql + " where id=?";
        final List<Object> args = getBeanInfo().getValues(t);
        args.add(t.id);
        getJdbcTemplate().update(sql, args.toArray());
    }

    /**
     * 通过condition更新
     *
     * @param condition 更新条件
     * @param vals      更新的字段名称和值、奇数位置必须为字段名、偶数位置必须为值
     */
    public void update(Condition condition, Object... vals) {
        final StringBuilder sql = new StringBuilder("update ");
        sql.append(getBeanInfo().tableName);
        sql.append(" set ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < vals.length; i += 2) {
            final String name = (String) vals[i];
            final Object val = vals[i + 1];
            if (i != 0) {
                sql.append(",");
            }
            sql.append(getBeanInfo().toColumnName(name));
            sql.append("=?");
            args.add(val);
        }
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            args.addAll(convertRes.paramList);
        }
        getJdbcTemplate().update(sql.toString(), args.toArray());
    }

    /**
     * 批量新增
     * 不会改变对象
     *
     * @param list
     */
    public void insertBatch(List<T> list) {
        final List<Object[]> argList = list.stream().map(e1 -> getBeanInfo().getValues(e1).toArray()).collect(Collectors.toList());
        getJdbcTemplate().batchUpdate(getBeanInfo().insertSql, argList);
    }

    /**
     * 批量更新
     *
     * @param list
     */
    public void updateBatch(List<T> list) {
        String sql = getBeanInfo().updateSql + " where id=?";

        final List<Object[]> argList = list.stream().map(e1 -> {
            final List<Object> args = getBeanInfo().getValues(e1);
            args.add(e1.id);
            return args.toArray();
        }).collect(Collectors.toList());

        getJdbcTemplate().batchUpdate(sql, argList);
    }

    /**
     * 根据id批量删除、使用批量删除
     *
     * @param ids
     */
    public void delete(long... ids) {
        if (ids.length == 1) {
            final String sql = "delete from " + getBeanInfo().tableName + " where id =?";
            getJdbcTemplate().update(sql, ids[0]);
        } else if (ids.length > 1) {
            final List<Object[]> argList = Arrays.stream(ids).mapToObj(e -> new Object[]{e}).collect(Collectors.toList());
            final String sql = "delete from " + getBeanInfo().tableName + " where id =?";
            getJdbcTemplate().batchUpdate(sql, argList);
        }
    }

    /**
     * 删除所有数据
     */
    public void delete() {
        delete((Condition) null);
    }

    /**
     * 根据条件删除
     */
    public void delete(Condition condition) {
        final ConvertRes convertRes = ConditionUtil.convertCondition(condition, beanInfo);
        final StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(getBeanInfo().tableName);
        final List<Object> paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = null;
        }
        if (paramList != null && !paramList.isEmpty()) {
            getJdbcTemplate().update(sql.toString(), paramList.toArray());
        } else {
            getJdbcTemplate().update(sql.toString());
        }
    }

    /**
     * 统计所有数量
     *
     * @return
     */
    public int count() {
        return count((ConvertRes) null);
    }

    /**
     * 统计数量
     *
     * @param condition
     * @return
     */
    public int count(Condition condition) {
        return count(ConditionUtil.convertCondition(condition, beanInfo));
    }

    private int count(ConvertRes convertRes) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(getBeanInfo().tableName);
        final List<Object> paramList;
        if (convertRes != null) {
            sql.append(" where ");
            sql.append(convertRes.sql);
            paramList = convertRes.paramList;
        } else {
            paramList = null;
        }

        if (paramList != null && !paramList.isEmpty()) {
            return getJdbcTemplate().queryForObject(sql.toString(), Integer.class, paramList.toArray());
        } else {
            return getJdbcTemplate().queryForObject(sql.toString(), Integer.class);
        }
    }

    private List<T> list(ConvertRes convertRes, Sort sort, int offset, int limit) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(getBeanInfo().tableName);
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
            return getJdbcTemplate().query(sql.toString(), new BeanPropertyRowMapper<>(getBeanInfo().clazz), paramList.toArray());
        } else {
            return getJdbcTemplate().query(sql.toString(), new BeanPropertyRowMapper<>(getBeanInfo().clazz));
        }
    }
}