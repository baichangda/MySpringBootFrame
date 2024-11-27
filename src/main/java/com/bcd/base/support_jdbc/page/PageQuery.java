package com.bcd.base.support_jdbc.page;


import com.bcd.base.exception.BaseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;


/**
 * 用于常规sql语句分页查询的帮助类
 * 可以{@link #PageQuery(String)}直接传入常规sql、会自动转换为countSql和pageSql、例如
 * select a,b,c from t_vehicle where vin =? and vehicle_type like ? order by create_time desc
 * 会生成
 * select count(*) from t_vehicle where vin =? and vehicle_type like ?
 * select a,b,c from t_vehicle where vin =? and vehicle_type like ? order by create_time desc limit ?,?
 * 转换逻辑参考方法{@link #toCountSql(String)}、{@link #toPageSql(String)}
 * sql必须满足如下条件
 * 1、格式必须为 select ... from ... order by ...
 * 2、order by语句可以没有
 * 3、不能以limit语句结尾
 *
 * 也可以{@link #PageQuery(String, String)}自己传入countSql和pageSql
 */
public final class PageQuery {
    public final String countSql;
    public final String pageSql;

    public PageQuery(String countSql, String pageSql) {
        this.countSql = countSql;
        this.pageSql = pageSql;
    }

    public PageQuery(String sql) {
        this.countSql = toCountSql(sql);
        this.pageSql = toPageSql(sql);
    }

    /**
     * 直接在sql结尾加上limit语句
     * @param sql
     * @return
     */
    public static String toPageSql(String sql) {
        return sql.toLowerCase().trim() + " limit ?,?";
    }

    /**
     * 寻找第一个select、from、将中间的语句替换为count(*)
     * 寻找最后一个order by、如果存在、则去掉order by及其后面的语句
     * @param sql
     * @return
     */
    public static String toCountSql(String sql) {
        String lowercase = sql.toLowerCase().trim();
        int i1 = lowercase.indexOf("select");
        int i2 = lowercase.indexOf("from");
        if (i1 == -1 || i2 == -1) {
            throw BaseException.get("toCountSql not support:\n{}", sql);
        }
        int i3 = lowercase.lastIndexOf("order by");
        return lowercase.substring(0, i1 + 6) +
                " count(*) " +
                (i3 == -1 ? lowercase.substring(i2) : lowercase.substring(i2, i3));
    }


    public <T> Page<T> query(JdbcTemplate jdbcTemplate, int pageNum, int pageSize, Class<T> clazz, Object... params) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        if (total == null || total == 0) {
            return Page.empty(pageRequest);
        } else {
            Object[] pageParams = new Object[params.length + 2];
            System.arraycopy(params, 0, pageParams, 0, params.length);
            pageParams[pageParams.length - 2] = pageNum * pageSize;
            pageParams[pageParams.length - 1] = pageSize;
            List<T> content = jdbcTemplate.query(pageSql, new BeanPropertyRowMapper<>(clazz), pageParams);
            return new PageImpl<>(content, pageRequest, total);
        }
    }


    public static void main(String[] args) {
        PageQuery pageQuery = new PageQuery("select a,b,c from t_vehicle where vin =? and vehicle_type like ? order by create_time desc");
        System.out.println(pageQuery.countSql);
        System.out.println(pageQuery.pageSql);
    }
}
