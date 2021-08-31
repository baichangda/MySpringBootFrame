package com.bcd.base.support_rdb.bean.info;

import com.bcd.base.support_rdb.anno.Unique;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
@SuppressWarnings("unchecked")
public class BeanInfo {
    /**
     * service的实体类
     */
    public Class clazz;

    /**
     * 实体类表名
     */
    public String tableName;

    /**
     * Unique 注解字段集合
     */
    public Boolean isCheckUnique;
    public List<Field> uniqueFieldList;

    /**
     * 主键字段
     */
    public Field pkField;


    public BeanInfo(Class clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        initTableName();
        initPkField();
        initUnique();
    }

    private void initTableName() {
        Table table = ((Table) clazz.getAnnotation(Table.class));
        this.tableName = table == null ? null : table.name();
    }

    private void initUnique() {
        uniqueFieldList = Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, Unique.class));
        uniqueFieldList.forEach(e -> e.setAccessible(true));
        if (uniqueFieldList.isEmpty()) {
            isCheckUnique = false;
        } else {
            isCheckUnique = true;
        }
    }

    /**
     * 初始化主键字段
     */
    private void initPkField() {
        pkField = FieldUtils.getFieldsWithAnnotation(clazz, Id.class)[0];
        pkField.setAccessible(true);
    }

}
