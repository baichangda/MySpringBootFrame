package com.base.code;

/**
 * Created by Administrator on 2017/7/28.
 */
public class ConfigProperties {
    private String tableName;
    private String moduleName;
    private String moduleDesc;
    private String dirPath;
    private boolean needCreateInfo=true;
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleDesc() {
        return moduleDesc;
    }

    public void setModuleDesc(String moduleDesc) {
        this.moduleDesc = moduleDesc;
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
}
