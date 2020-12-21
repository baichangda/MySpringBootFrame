package com.bcd.mongodb.bean.info;

import com.bcd.mongodb.anno.Unique;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
     * Unique 注解字段集合
     */
    public Boolean isCheckUnique;
    public List<Field> uniqueFieldList;

    /**
     * 主键字段
     */
    public Field pkField;
    public String pkFieldName;

    /**
     * bean所属collection
     */
    public String collection;


    public BeanInfo(Class clazz){
        this.clazz = clazz;
        init();
    }

    private void init(){
        initPkField();
        initUnique();
        initCollection();
    }

    public void initCollection(){
        collection= ((Document)clazz.getAnnotation(Document.class)).collection();
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
     * 初始化主键字段
     */
    public void initPkField(){
        pkField= FieldUtils.getFieldsWithAnnotation(clazz,Id.class)[0];
        pkField.setAccessible(true);
        pkFieldName=pkField.getName();
    }

}
