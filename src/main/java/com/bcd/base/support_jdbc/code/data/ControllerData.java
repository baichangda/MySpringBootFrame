package com.bcd.base.support_jdbc.code.data;

import java.util.List;

public class ControllerData {
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
     * controller映射路径
     */
    public String requestMappingPre;

    /**
     * 字段集合
     */
    public List<BeanField> fieldList;

    /**
     * 是否需要保存方法验证
     */
    public boolean validateSaveParam = true;

    /**
     * 主键字段
     */
    public BeanField pkField;

}
