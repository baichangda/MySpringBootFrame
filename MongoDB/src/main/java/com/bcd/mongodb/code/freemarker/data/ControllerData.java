package com.bcd.mongodb.code.freemarker.data;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ControllerData {
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
     * controller映射路径
     */
    private String requestMappingPre;

    /**
     * 字段集合
     */
    private List<BeanField> fieldList;

    /**
     * 是否需要保存方法验证
     */
    private boolean validateSaveParam=true;

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

    public String getPackagePre() {
        return packagePre;
    }

    public void setPackagePre(String packagePre) {
        this.packagePre = packagePre;
    }

    public String getModuleNameCN() {
        return moduleNameCN;
    }

    public void setModuleNameCN(String moduleNameCN) {
        this.moduleNameCN = moduleNameCN;
    }

    public String getRequestMappingPre() {
        return requestMappingPre;
    }

    public void setRequestMappingPre(String requestMappingPre) {
        this.requestMappingPre = requestMappingPre;
    }

    public List<BeanField> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<BeanField> fieldList) {
        this.fieldList = fieldList;
    }

    public boolean isValidateSaveParam() {
        return validateSaveParam;
    }

    public void setValidateSaveParam(boolean validateSaveParam) {
        this.validateSaveParam = validateSaveParam;
    }

    public String getPkType() {
        return pkType;
    }

    public void setPkType(String pkType) {
        this.pkType = pkType;
    }
}
