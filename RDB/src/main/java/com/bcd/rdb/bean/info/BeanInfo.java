package com.bcd.rdb.bean.info;

import com.bcd.rdb.anno.Unique;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
@SuppressWarnings("unchecked")
public class BeanInfo{
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
     * manyToManyFieldList 多对多字段集合
     * oneToManyFieldList 一对多字段集合
     * manyToOneFieldList 多对一字段集合
     * oneToOneFieldList 一对一字段集合
     */
    public List<Field> manyToManyFieldList;
    public List<Field> oneToManyFieldList;
    public List<Field> manyToOneFieldList;
    public List<Field> oneToOneFieldList;

    /**
     * 主键字段
     */
    public Field pkField;


    public BeanInfo(Class clazz){
        this.clazz = clazz;
        init();
    }

    private void init(){
        initTableName();
        initPkField();
        initUnique();
        initJPAAnno();
    }

    public void initTableName(){
        Table table= ((Table)clazz.getAnnotation(Table.class));
        this.tableName= table==null?null:table.name();
    }

    public void initUnique(){
        uniqueFieldList= Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, Unique.class));
        uniqueFieldList.forEach(e->e.setAccessible(true));
        if(uniqueFieldList.isEmpty()){
            isCheckUnique =false;
        }else{
            isCheckUnique =true;
        }
    }


    /**
     * 初始化 JPA 注解
     */
    public void initJPAAnno(){
        manyToManyFieldList=Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, ManyToMany.class));
        oneToManyFieldList=Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, OneToMany.class));
        manyToOneFieldList=Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, ManyToOne.class));
        oneToOneFieldList=Arrays.asList(FieldUtils.getFieldsWithAnnotation(clazz, OneToOne.class));
        manyToManyFieldList.forEach(e->e.setAccessible(true));
        oneToManyFieldList.forEach(e->e.setAccessible(true));
        manyToOneFieldList.forEach(e->e.setAccessible(true));
        oneToOneFieldList.forEach(e->e.setAccessible(true));
    }

    /**
     * 初始化主键字段
     */
    public void initPkField(){
        pkField= FieldUtils.getFieldsWithAnnotation(clazz, Id.class)[0];
        pkField.setAccessible(true);
    }

}
