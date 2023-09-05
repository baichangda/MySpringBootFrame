package com.bcd.base.support_jdbc.service;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.anno.Transient;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class BeanInfo<T> {
    /**
     * service的实体类
     */
    public final Class<T> clazz;

    /**
     * 实体类表名
     */
    public final String tableName;

    /**
     * 所有表列对应的字段
     */
    private final List<FieldInfo> columnFieldList;
    private final List<FieldInfo> columnFieldList_noId;


    private final Map<String, String> fieldNameOrColumnName_columnName;

    /**
     * 新增的sql(不包含id)
     * <p>
     * insert into tableName(?,...) values(?,...)
     */
    public final String insertSql_noId;
    public final String insertSql;

    /**
     * 更新的sql(不包含id)
     * <p>
     * update tableName set xxx=? ... where id=?
     */
    public final String updateSql_noId;

    public BeanInfo(Class<T> clazz) {
        this.clazz = clazz;

        Table table = clazz.getAnnotation(Table.class);
        tableName = table == null ? null : table.value();

        final Field[] allFields = FieldUtils.getAllFields(clazz);
        columnFieldList_noId = new ArrayList<>();
        columnFieldList = new ArrayList<>();
        fieldNameOrColumnName_columnName = new HashMap<>();
        for (Field f : allFields) {
            if (f.getAnnotation(Transient.class) == null && !Modifier.isStatic(f.getModifiers())) {
                final String fieldName = f.getName();
                final FieldInfo fieldInfo = new FieldInfo(f);
                if (!fieldName.equals("id")) {
                    columnFieldList_noId.add(fieldInfo);
                }
                columnFieldList.add(fieldInfo);
                fieldNameOrColumnName_columnName.put(fieldInfo.fieldName, fieldInfo.columnName);
                fieldNameOrColumnName_columnName.put(fieldInfo.columnName, fieldInfo.columnName);
            }
        }

        StringJoiner sj1 = new StringJoiner(",");
        StringJoiner sj2 = new StringJoiner(",");
        StringJoiner sj3 = new StringJoiner(",");
        for (FieldInfo fieldInfo : columnFieldList_noId) {
            final String columnName = fieldInfo.columnName;
            sj1.add(columnName);
            sj2.add("?");
            sj3.add(columnName + "=?");
        }
        insertSql_noId = "insert into " + tableName + "(" + sj1 + ") values(" + sj2 + ")";
        insertSql = "insert into " + tableName + "(id," + sj1 + ") values(?," + sj2 + ")";
        updateSql_noId = "update " + tableName + " set " + sj3;
    }

    public List<Object> getValues_noId(T t) {
        try {
            List<Object> args = new ArrayList<>();
            for (FieldInfo fieldInfo : columnFieldList_noId) {
                final Object v = fieldInfo.field.get(t);
                args.add(v);
            }
            return args;
        } catch (IllegalAccessException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public List<Object> getValues(T t) {
        try {
            List<Object> args = new ArrayList<>();
            for (FieldInfo fieldInfo : columnFieldList) {
                final Object v = fieldInfo.field.get(t);
                args.add(v);
            }
            return args;
        } catch (IllegalAccessException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public String toColumnName(String fieldNameOrColumnName) {
        final String columnName = fieldNameOrColumnName_columnName.get(fieldNameOrColumnName);
        if (columnName == null) {
            throw BaseRuntimeException.getException("bean[{}] tableName[{}] toColumnName[{}] null", clazz.getName(), tableName, fieldNameOrColumnName);
        }
        return columnName;
    }
}
