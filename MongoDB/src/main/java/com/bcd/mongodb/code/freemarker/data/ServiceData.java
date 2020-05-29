package com.bcd.mongodb.code.freemarker.data;

public class ServiceData {
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
     * 主键类型
     */
    private String pkType;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleNameCN() {
        return moduleNameCN;
    }

    public void setModuleNameCN(String moduleNameCN) {
        this.moduleNameCN = moduleNameCN;
    }

    public String getPackagePre() {
        return packagePre;
    }

    public void setPackagePre(String packagePre) {
        this.packagePre = packagePre;
    }

    public String getPkType() {
        return pkType;
    }

    public void setPkType(String pkType) {
        this.pkType = pkType;
    }
}
