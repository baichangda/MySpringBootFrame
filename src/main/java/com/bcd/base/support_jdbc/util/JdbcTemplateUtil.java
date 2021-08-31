package com.bcd.base.support_jdbc.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.rowmapper.MyColumnMapRowMapper;
import com.bcd.base.support_jdbc.sql.BatchCreateSqlResult;
import com.bcd.base.support_jdbc.sql.BatchUpdateSqlResult;
import com.bcd.base.support_jdbc.sql.SqlListResult;
import com.bcd.base.support_jdbc.sql.SqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JdbcTemplateUtil {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    /**
     * 分页查询
     *
     * @param sql      查询结果集sql(不带limit)
     * @param pageable 分页对象参数
     * @param params   参数
     * @return
     */
    public Page<Map<String, Object>> pageBySql(String sql, Pageable pageable, Object... params) {
        SqlListResult countSql = SqlUtil.replace_nullParam_count(sql, params);
        Integer count;
        if (countSql.getParamList().isEmpty()) {
            count = jdbcTemplate.queryForObject(countSql.getSql(), Integer.class);
        } else {
            count = jdbcTemplate.queryForObject(countSql.getSql(), Integer.class, countSql.getParamList().toArray(new Object[0]));
        }
        if (count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } else {
            SqlListResult dataSql = SqlUtil.replace_nullParam_limit(sql, pageable.getPageNumber(), pageable.getPageSize(), params);
            List<Map<String, Object>> dataList;
            if (dataSql.getParamList().isEmpty()) {
                dataList = jdbcTemplate.query(dataSql.getSql(), MyColumnMapRowMapper.ROW_MAPPER);
            } else {
                dataList = jdbcTemplate.query(dataSql.getSql(), MyColumnMapRowMapper.ROW_MAPPER, dataSql.getParamList().toArray(new Object[0]));
            }
            return new PageImpl<>(dataList, pageable, count);
        }
    }

    /**
     * 分页查询
     *
     * @param sql      查询结果集sql(不带limit)
     * @param pageable 分页对象参数
     * @param params   参数
     * @return
     */
    public <T> Page<T> pageBySql(String sql, Pageable pageable, Class<T> clazz, Object... params) {
        SqlListResult countSql = SqlUtil.replace_nullParam_count(sql, params);
        Integer count;
        if (countSql.getParamList().isEmpty()) {
            count = jdbcTemplate.queryForObject(countSql.getSql(), Integer.class);
        } else {
            count = jdbcTemplate.queryForObject(countSql.getSql(), Integer.class, countSql.getParamList().toArray(new Object[0]));
        }
        if (count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } else {
            SqlListResult dataSql = SqlUtil.replace_nullParam_limit(sql, pageable.getPageNumber(), pageable.getPageSize(), params);
            List<T> dataList;
            if (dataSql.getParamList().isEmpty()) {
                dataList = jdbcTemplate.query(dataSql.getSql(), new BeanPropertyRowMapper<>(clazz));
            } else {
                dataList = jdbcTemplate.query(dataSql.getSql(), new BeanPropertyRowMapper<>(clazz), dataSql.getParamList().toArray(new Object[0]));
            }
            return new PageImpl<>(dataList, pageable, count);
        }
    }


    /**
     * 批量新增、不做唯一校验、所有的对象采用新增方式
     *
     * @param list
     * @param ignoreFields
     */
    @Transactional
    public void insertBatch(List list, String pkFieldName, String tableName, String... ignoreFields) {
        if (list.isEmpty()) {
            return;
        }
        Set<String> ignoreFieldSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        //忽略主键字段、主键一般为自增
        ignoreFieldSet.add(pkFieldName);
        BatchCreateSqlResult batchCreateSqlResult = SqlUtil.generateBatchCreateResult(list, tableName, null, ignoreFieldSet.toArray(new String[0]));
        jdbcTemplate.batchUpdate(batchCreateSqlResult.getSql(), batchCreateSqlResult.getParamList());
    }

    /**
     * 批量更新、不做唯一校验、所有的对象采用更新方式、所有对象主键不能为null
     *
     * @param list
     * @param ignoreFields
     */
    @Transactional
    public void updateBatch(List list, String pkFieldName, String tableName, String... ignoreFields) {
        if (list.isEmpty()) {
            return;
        }
        Set<String> ignoreFieldSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        //忽略主键字段
        ignoreFieldSet.add(pkFieldName);
        BatchUpdateSqlResult batchUpdateSqlResult = SqlUtil.generateBatchUpdateResult(list, tableName, null, ignoreFieldSet.toArray(new String[0]));
        jdbcTemplate.batchUpdate(batchUpdateSqlResult.getSql(), batchUpdateSqlResult.getParamList());
    }

    /**
     * 批量新增/更新、不做唯一校验、根据主键是否有值来区分新增还是更新
     *
     * @param list
     * @param ignoreFields
     */
    @Transactional
    public void saveBatch(List list, String pkFieldName, String tableName, String... ignoreFields) {
        if (list.isEmpty()) {
            return;
        }
        List insertList = new ArrayList<>();
        List updateList = new ArrayList<>();
        Field pkField = null;
        try {
            for (Object t : list) {
                if (pkField == null) {
                    pkField = t.getClass().getField(pkFieldName);
                    pkField.setAccessible(true);
                }
                if (pkField.get(t) == null) {
                    insertList.add(t);
                } else {
                    updateList.add(t);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw BaseRuntimeException.getException(ex);
        }
        if (!insertList.isEmpty()) {
            insertBatch(insertList, pkFieldName, tableName, ignoreFields);
        }
        if (!updateList.isEmpty()) {
            updateBatch(updateList, pkFieldName, tableName, ignoreFields);
        }
    }
}
