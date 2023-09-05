package com.bcd.base.support_jdbc.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.support_jdbc.bean.SuperBaseBean;
import com.bcd.base.support_jdbc.condition.ConditionUtil;
import com.bcd.base.support_jdbc.condition.ConvertRes;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.StringJoiner;
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
     * 新增
     * 如果设置了id、即会按照id新增、否则自增id
     * 所有属性都会作为参数设置、即使是null
     * @param t
     */
    public void insert(T t) {
        if (t.id == null) {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(conn -> {
                final PreparedStatement ps = conn.prepareStatement(getBeanInfo().insertSql_noId, Statement.RETURN_GENERATED_KEYS);
                final ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(getBeanInfo().getValues_noId(t).toArray());
                argumentPreparedStatementSetter.setValues(ps);
                return ps;
            }, keyHolder);
            t.id = keyHolder.getKey().longValue();
        } else {
            getJdbcTemplate().update(getBeanInfo().insertSql, getBeanInfo().getValues(t));
        }

    }

    /**
     * 根据id更新
     * 所有属性都会作为参数设置、即使是null
     * @param t
     */
    public void update(T t) {
        final String sql = getBeanInfo().updateSql_noId + " where id=?";
        final List<Object> args = getBeanInfo().getValues_noId(t);
        args.add(t.id);
        getJdbcTemplate().update(sql, args.toArray());
    }

    /**
     * 根据参数对新增、只新增部分字段
     * @param params
     */
    public void insert(ParamPairs... params) {
        if (params.length == 0) {
            return;
        }
        StringJoiner sj1 = new StringJoiner(",");
        StringJoiner sj2 = new StringJoiner(",");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            ParamPairs param = params[i];
            sj1.add(getBeanInfo().toColumnName(param.field()));
            sj2.add("?");
            args.add(param.val());
        }
        String sql = "insert " + getBeanInfo().tableName + "(" + sj1 + ") values(" + sj2 + ")";
        getJdbcTemplate().update(sql, args.toArray());
    }

    /**
     * 根据id、参数对更新
     * 只会更新部分字段
     * @param id
     * @param params
     */
    public void update(long id, ParamPairs... params) {
        if (params.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder("update ");
        sql.append(getBeanInfo().tableName);
        sql.append(" set ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            ParamPairs param = params[i];
            if (i != 0) {
                sql.append(",");
            }
            sql.append(getBeanInfo().toColumnName(param.field()));
            sql.append("=?");
            args.add(param.val());
        }
        sql.append(" where id=?");
        args.add(id);
        getJdbcTemplate().update(sql.toString(), args.toArray());
    }

    /**
     * 通过condition、参数对更新
     * 只会更新部分字段
     * @param condition 更新条件
     * @param params    更新值
     */
    public void update(Condition condition, ParamPairs... params) {
        if (params.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder("update ");
        sql.append(getBeanInfo().tableName);
        sql.append(" set ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            ParamPairs param = params[i];
            if (i != 0) {
                sql.append(",");
            }
            sql.append(getBeanInfo().toColumnName(param.field()));
            sql.append("=?");
            args.add(param.val());
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
        if (list.isEmpty()) {
            return;
        }
        T t = list.get(0);
        if (t.id == null) {
            final List<Object[]> argList = list.stream().map(e1 -> getBeanInfo().getValues_noId(e1).toArray()).collect(Collectors.toList());
            getJdbcTemplate().batchUpdate(getBeanInfo().insertSql_noId, argList);
        } else {
            final List<Object[]> argList = list.stream().map(e1 -> getBeanInfo().getValues(e1).toArray()).collect(Collectors.toList());
            getJdbcTemplate().batchUpdate(getBeanInfo().insertSql, argList);
        }
    }


    /**
     * 批量更新
     *
     * @param list
     */
    public void updateBatch(List<T> list) {
        String sql = getBeanInfo().updateSql_noId + " where id=?";
        final List<Object[]> argList = list.stream().map(e1 -> {
            final List<Object> args = getBeanInfo().getValues_noId(e1);
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