package com.bcd.rdb.code.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class BeanData {

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 模块中文名
     */
    private String moduleNameCN;

    /**
     * 包路径
     */
    private String packagePre;

    /**
     * 父类
     * 1: #{@link com.bcd.rdb.bean.BaseBean}
     * 2: #{@link com.bcd.rdb.bean.SuperBaseBean}
     */
    private int superBeanType=2;

    /**
     * 主键类型
     */
    private String pkType;

    /**
     * 映射数据库表名
     */
    private String tableName;

    /**
     * 字段集合
     */
    private List<BeanField> fieldList;

}

