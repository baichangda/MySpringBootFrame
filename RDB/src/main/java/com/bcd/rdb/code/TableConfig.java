package com.bcd.rdb.code;


import java.util.HashMap;
import java.util.Map;

public class TableConfig {
    //模块名(英文)
    public String moduleName;
    //模块名(中文)
    public String moduleNameCN;
    //表名
    public String tableName;
    //是否需要创建信息(默认需要)
    public boolean needCreateInfo = true;
    //是否创建bean文件(默认是)
    public boolean needCreateBeanFile=true;
    //是否创建repository文件(默认是)
    public boolean needCreateRepositoryFile=true;
    //是否创建service文件(默认是)
    public boolean needCreateServiceFile=true;
    //是否创建controller文件(默认是)
    public boolean needCreateControllerFile=true;
    //是否需要创建bean时候加入字段验证注解
    public boolean isNeedBeanValidate=true;
    //是否需要加上controller save方法的验证注解
    public boolean isNeedParamValidate=true;
    //当前生成controller requestMapping匹配路径前缀
    public String requestMappingPre;

    //存储即将替换模版值数据
    public Map<String,Object> valueMap = new HashMap<>();

    //存储运行中的数据
    public Map<String,Object> dataMap = new HashMap<>();

    public Config config;

    public TableConfig(String moduleName, String moduleNameCN, String tableName) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.tableName = tableName;
    }

    public TableConfig(String moduleName, String moduleNameCN, String tableName,boolean needCreateInfo,
                       boolean needCreateBeanFile,boolean needCreateRepositoryFile,boolean needCreateServiceFile,boolean needCreateControllerFile,boolean isNeedBeanValidate,boolean isNeedParamValidate,
                       String requestMappingPre) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.tableName = tableName;
        this.needCreateInfo=needCreateInfo;
        this.needCreateBeanFile=needCreateBeanFile;
        this.needCreateRepositoryFile=needCreateRepositoryFile;
        this.needCreateServiceFile=needCreateServiceFile;
        this.needCreateControllerFile=needCreateControllerFile;
        this.isNeedBeanValidate=isNeedBeanValidate;
        this.isNeedParamValidate=isNeedParamValidate;
        this.requestMappingPre=requestMappingPre;
    }

    public String getModuleName() {
        return moduleName;
    }

    public TableConfig setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getModuleNameCN() {
        return moduleNameCN;
    }

    public TableConfig setModuleNameCN(String moduleNameCN) {
        this.moduleNameCN = moduleNameCN;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public TableConfig setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public boolean isNeedCreateInfo() {
        return needCreateInfo;
    }

    public TableConfig setNeedCreateInfo(boolean needCreateInfo) {
        this.needCreateInfo = needCreateInfo;
        return this;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public TableConfig setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        return this;
    }

    public Map<String, Object> getValueMap() {
        return valueMap;
    }

    public TableConfig setValueMap(Map<String, Object> valueMap) {
        this.valueMap = valueMap;
        return this;
    }

    public Config getConfig() {
        return config;
    }

    public TableConfig setConfig(Config config) {
        this.config = config;
        return this;
    }

    public boolean isNeedCreateBeanFile() {
        return needCreateBeanFile;
    }

    public TableConfig setNeedCreateBeanFile(boolean needCreateBeanFile) {
        this.needCreateBeanFile = needCreateBeanFile;
        return this;
    }

    public boolean isNeedCreateRepositoryFile() {
        return needCreateRepositoryFile;
    }

    public TableConfig setNeedCreateRepositoryFile(boolean needCreateRepositoryFile) {
        this.needCreateRepositoryFile = needCreateRepositoryFile;
        return this;
    }

    public boolean isNeedCreateServiceFile() {
        return needCreateServiceFile;
    }

    public TableConfig setNeedCreateServiceFile(boolean needCreateServiceFile) {
        this.needCreateServiceFile = needCreateServiceFile;
        return this;
    }

    public boolean isNeedCreateControllerFile() {
        return needCreateControllerFile;
    }

    public TableConfig setNeedCreateControllerFile(boolean needCreateControllerFile) {
        this.needCreateControllerFile = needCreateControllerFile;
        return this;
    }

    public boolean isNeedBeanValidate() {
        return isNeedBeanValidate;
    }

    public TableConfig setNeedBeanValidate(boolean needBeanValidate) {
        isNeedBeanValidate = needBeanValidate;
        return this;
    }

    public boolean isNeedParamValidate() {
        return isNeedParamValidate;
    }

    public TableConfig setNeedParamValidate(boolean needParamValidate) {
        isNeedParamValidate = needParamValidate;
        return this;
    }

    public String getRequestMappingPre() {
        return requestMappingPre;
    }

    public TableConfig setRequestMappingPre(String requestMappingPre) {
        this.requestMappingPre = requestMappingPre;
        return this;
    }
}