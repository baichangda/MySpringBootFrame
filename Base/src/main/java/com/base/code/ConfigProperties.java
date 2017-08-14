package com.base.code;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/28.
 */
public class ConfigProperties {
    private String dirPath;
    private boolean needCreateInfo=true;
    private String moduleName;
    private String moduleNameCN;
    private String tableName;
    private Map<String,String> valueMap=new HashMap<>();
    private Map<String,Object> dataMap=new HashMap<>();

    public ConfigProperties(String dirPath, String moduleName, String tableName,String moduleNameCN) {
        this.dirPath = dirPath;
        this.moduleName = moduleName;
        this.tableName = tableName;
        this.moduleNameCN=moduleNameCN;
    }

    public ConfigProperties(String dirPath, String moduleName, String tableName,String moduleNameCN,boolean needCreateInfo) {
        this(dirPath, moduleName, tableName, moduleNameCN);
        this.needCreateInfo=needCreateInfo;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public boolean isNeedCreateInfo() {
        return needCreateInfo;
    }

    public void setNeedCreateInfo(boolean needCreateInfo) {
        this.needCreateInfo = needCreateInfo;
    }

    public Map<String, String> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModuleNameCN() {
        return moduleNameCN;
    }

    public void setModuleNameCN(String moduleNameCN) {
        this.moduleNameCN = moduleNameCN;
    }
}
