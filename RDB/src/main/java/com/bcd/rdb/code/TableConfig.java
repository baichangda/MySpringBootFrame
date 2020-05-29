package com.bcd.rdb.code;



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
    public boolean needValidateBeanField=true;
    //是否需要加上controller save方法的验证注解
    public boolean needValidateSaveParam=true;

    public Config config;

    public TableConfig(String moduleName, String moduleNameCN, String tableName) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.tableName = tableName;
    }

    public TableConfig(String moduleName, String moduleNameCN, String tableName,boolean needCreateInfo,
                       boolean needCreateBeanFile,boolean needCreateRepositoryFile,boolean needCreateServiceFile,boolean needCreateControllerFile,boolean needValidateBeanField,boolean needValidateSaveParam) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.tableName = tableName;
        this.needCreateInfo=needCreateInfo;
        this.needCreateBeanFile=needCreateBeanFile;
        this.needCreateRepositoryFile=needCreateRepositoryFile;
        this.needCreateServiceFile=needCreateServiceFile;
        this.needCreateControllerFile=needCreateControllerFile;
        this.needValidateBeanField=needValidateBeanField;
        this.needValidateSaveParam=needValidateSaveParam;
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

    public boolean isNeedValidateBeanField() {
        return needValidateBeanField;
    }

    public TableConfig setNeedValidateBeanField(boolean needValidateBeanField) {
        this.needValidateBeanField = needValidateBeanField;
        return this;
    }

    public boolean isNeedValidateSaveParam() {
        return needValidateSaveParam;
    }

    public TableConfig setNeedValidateSaveParam(boolean needValidateSaveParam) {
        this.needValidateSaveParam = needValidateSaveParam;
        return this;
    }
}