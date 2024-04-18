package com.bcd.base.support_jdbc.service;

import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.anno.Transient;
import com.bcd.base.support_jdbc.anno.Unique;
import com.bcd.base.support_jdbc.bean.BaseBean;
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

    /**
     * 是否在新增时候自动设置创建信息
     */
    public final boolean autoSetCreateInfoBeforeInsert;
    /**
     * 是否在更新时候自动设置更新信息
     */
    public final boolean autoSetUpdateInfoBeforeUpdate;
    /**
     * 唯一字段集合
     */
    public final List<UniqueInfo> uniqueInfoList;

    public BeanInfo(Class<T> clazz) {
        this.clazz = clazz;

        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw MyException.get("class[{}] must has annotation @Table", clazz.getName());
        }
        tableName = table.value();

        if (BaseBean.class.isAssignableFrom(clazz)) {
            autoSetCreateInfoBeforeInsert = table.autoSetCreateInfoBeforeInsert();
            autoSetUpdateInfoBeforeUpdate = table.autoSetUpdateInfoBeforeUpdate();
        } else {
            autoSetCreateInfoBeforeInsert = false;
            autoSetUpdateInfoBeforeUpdate = false;
        }

        final Field[] allFields = FieldUtils.getAllFields(clazz);
        columnFieldList_noId = new ArrayList<>();
        columnFieldList = new ArrayList<>();
        uniqueInfoList = new ArrayList<>();
        fieldNameOrColumnName_columnName = new HashMap<>();

        FieldInfo idFieldInfo = null;
        for (Field f : allFields) {
            if (f.getAnnotation(Transient.class) == null && !Modifier.isStatic(f.getModifiers())) {
                final FieldInfo fieldInfo = new FieldInfo(f);
                if (f.getName().equals("id")) {
                    idFieldInfo = fieldInfo;
                } else {
                    columnFieldList_noId.add(fieldInfo);
                }
                fieldNameOrColumnName_columnName.put(fieldInfo.fieldName, fieldInfo.columnName);
                fieldNameOrColumnName_columnName.put(fieldInfo.columnName, fieldInfo.columnName);
                Unique unique = f.getAnnotation(Unique.class);
                if (unique != null) {
                    uniqueInfoList.add(new UniqueInfo(fieldInfo, unique, tableName));
                }
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
        columnFieldList.add(idFieldInfo);
        columnFieldList.addAll(columnFieldList_noId);
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
            throw MyException.get(e);
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
            throw MyException.get(e);
        }
    }

    public String toColumnName(String fieldNameOrColumnName) {
        final String columnName = fieldNameOrColumnName_columnName.get(fieldNameOrColumnName);
        if (columnName == null) {
            throw MyException.get("bean[{}] tableName[{}] toColumnName[{}] null", clazz.getName(), tableName, fieldNameOrColumnName);
        }
        return columnName;
    }
}
