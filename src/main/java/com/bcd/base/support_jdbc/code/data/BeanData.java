package com.bcd.base.support_jdbc.code.data;


import java.util.List;

public class BeanData {

    /**
     * 模块名
     */
    public String moduleName;

    /**
     * 模块中文名
     */
    public String moduleNameCN;

    /**
     * 包路径
     */
    public String packagePre;

    /**
     * 父类
     * 1: #{@link com.bcd.base.support_jpa.bean.BaseBean}
     * 2: #{@link com.bcd.base.support_jpa.bean.SuperBaseBean}
     */
    public int superBeanType;

    /**
     * 主键类型
     */
    public String pkType;

    /**
     * 映射数据库表名
     */
    public String tableName;

    /**
     * 字段集合
     */
    public List<BeanField> fieldList;

}

