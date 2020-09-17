package com.bcd.mongodb.bean.info;

import com.bcd.mongodb.anno.Unique;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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


    public BeanInfo(Class clazz){
        this.clazz = clazz;
        init();
    }

    private void init(){
        initPkField();
        initUnique();
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
    }


    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Boolean getCheckUnique() {
        return isCheckUnique;
    }

    public void setCheckUnique(Boolean checkUnique) {
        isCheckUnique = checkUnique;
    }

    public List<Field> getUniqueFieldList() {
        return uniqueFieldList;
    }

    public void setUniqueFieldList(List<Field> uniqueFieldList) {
        this.uniqueFieldList = uniqueFieldList;
    }

    public Field getPkField() {
        return pkField;
    }

    public void setPkField(Field pkField) {
        this.pkField = pkField;
    }
}
