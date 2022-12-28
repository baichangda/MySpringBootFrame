package com.bcd.base.support_jdbc.service;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.anno.Transient;
import com.bcd.base.util.StringUtil;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class BeanInfo<T> {
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
    private final Map<String, String> fieldNameOrColumnName_columnName;

    /**
     * 新增的sql
     * <p>
     * insert into tableName(?,...) values(?,...)
     */
    public final String insertSql;

    /**
     * 更新的sql
     * <p>
     * update tableName set xxx=? ... where id=?
     */
    public final String updateSql;

    public BeanInfo(Class clazz) {
        this(clazz, null);
    }

    public BeanInfo(Class clazz, Function<List<FieldInfo>, List<FieldInfo>> function) {
        this.clazz = clazz;

        Table table = (Table) clazz.getAnnotation(Table.class);
        tableName = table == null ? null : table.value();

        final List<FieldInfo> tempList = new ArrayList<>();
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        for (Field f : allFields) {
            final String fieldName = f.getName();
            if (!fieldName.equals("id") && f.getAnnotation(Transient.class) == null && !Modifier.isStatic(f.getModifiers())) {
                final FieldInfo fieldInfo = new FieldInfo(f);
                tempList.add(fieldInfo);
            }
        }
        if (function == null) {
            columnFieldList = tempList;
        } else {
            columnFieldList = function.apply(tempList);
        }

        fieldNameOrColumnName_columnName = new HashMap<>();
        for (FieldInfo fieldInfo : columnFieldList) {
            fieldNameOrColumnName_columnName.put(fieldInfo.fieldName, fieldInfo.columnName);
            fieldNameOrColumnName_columnName.put(fieldInfo.columnName, fieldInfo.columnName);
        }

        StringJoiner sj1 = new StringJoiner(",");
        StringJoiner sj2 = new StringJoiner(",");
        StringJoiner sj3 = new StringJoiner(",");
        for (FieldInfo fieldInfo : columnFieldList) {
            final String columnName = fieldInfo.columnName;
            sj1.add(columnName);
            sj2.add("?");
            sj3.add(columnName + "=?");
        }
        insertSql = "insert into " + tableName + "(" + sj1 + ") values(" + sj2 + ")";
        updateSql = "update " + tableName + " set " + sj3;
    }

    public static class FieldInfo {
        public final String fieldName;
        public final String columnName;

        public final Field field;

        public FieldInfo(Field field) {
            this.field = field;
            this.fieldName = field.getName();
            this.columnName = StringUtil.toFirstSplitWithUpperCase(this.fieldName, '_');
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
