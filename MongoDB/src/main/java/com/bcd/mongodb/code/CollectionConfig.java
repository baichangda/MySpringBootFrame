package com.bcd.mongodb.code;

import java.util.HashMap;
import java.util.Map;

public class CollectionConfig {
    //模块名(英文)
    public String moduleName;
    //模块名(中文)
    public String moduleNameCN;
    //类名
    public Class clazz;
    //是否需要加上controller save方法的验证注解
    public boolean isNeedParamValidate=false;
    //当前生成controller requestMapping匹配路径前缀
    public String requestMappingPre;
    //存储运行中的数据
    public Map<String,Object> dataMap = new HashMap<>();
    //存储即将替换模版值数据
    public Map<String,Object> valueMap = new HashMap<>();


    public Config config;

    public CollectionConfig(String moduleName, String moduleNameCN, Class clazz) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.clazz = clazz;
    }

    public CollectionConfig(String moduleName, String moduleNameCN, Class clazz,boolean isNeedParamValidate,String requestMappingPre) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.clazz = clazz;
        this.isNeedParamValidate=isNeedParamValidate;
        this.requestMappingPre=requestMappingPre;
    }

    public String getModuleName() {
        return moduleName;
    }

    public CollectionConfig setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getModuleNameCN() {
        return moduleNameCN;
    }

    public CollectionConfig setModuleNameCN(String moduleNameCN) {
        this.moduleNameCN = moduleNameCN;
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public CollectionConfig setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public CollectionConfig setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
        return this;
    }

    public Map<String, Object> getValueMap() {
        return valueMap;
    }

    public CollectionConfig setValueMap(Map<String, Object> valueMap) {
        this.valueMap = valueMap;
        return this;
    }

    public Config getConfig() {
        return config;
    }

    public CollectionConfig setConfig(Config config) {
        this.config = config;
        return this;
    }

    public boolean isNeedParamValidate() {
        return isNeedParamValidate;
    }

    public CollectionConfig setNeedParamValidate(boolean needParamValidate) {
        isNeedParamValidate = needParamValidate;
        return this;
    }

    public String getRequestMappingPre() {
        return requestMappingPre;
    }

    public CollectionConfig setRequestMappingPre(String requestMappingPre) {
        this.requestMappingPre = requestMappingPre;
        return this;
    }
}