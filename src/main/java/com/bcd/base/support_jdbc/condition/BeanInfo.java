package com.bcd.base.support_jdbc.condition;

import com.bcd.base.support_jdbc.anno.Table;
import com.bcd.base.support_jdbc.anno.Transient;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BeanInfo<T> {
    /**
     * service的实体类
     */
    public final Class<T> clazz;

    /**
     * 实体类表名
     */
    public String tableName;

    public String insertSql;
    public String updateSql;
    public List<Field> fieldList;


    public BeanInfo(Class clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        initTableName();
        initSql();
    }


    private void initSql() {
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        fieldList = Arrays.stream(allFields).filter(e -> !e.getName().equals("id") && e.getAnnotation(Transient.class) == null && !Modifier.isStatic(e.getModifiers())).collect(Collectors.toList());
        StringJoiner sj1 = new StringJoiner(",");
        StringJoiner sj2 = new StringJoiner(",");
        StringJoiner sj3 = new StringJoiner(",");
        for (Field field : fieldList) {
            sj1.add(field.getName());
            sj2.add("?");
            sj3.add(field.getName() + "=?");
        }
        insertSql = "insert into " + tableName + "(" + sj1 + ") values(" + sj2 + ")";
        updateSql = "update " + tableName + " set " + sj3;
    }

    private void initTableName() {
        Table table = clazz.getAnnotation(Table.class);
        this.tableName = table == null ? null : table.value();
    }

}
