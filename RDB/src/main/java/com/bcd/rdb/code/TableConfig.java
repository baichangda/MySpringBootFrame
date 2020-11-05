package com.bcd.rdb.code;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableConfig {
    //模块名(英文)
    private String moduleName;
    //模块名(中文)
    private String moduleNameCN;
    //表名
    private String tableName;
    //是否需要创建信息(默认需要)
    private boolean needCreateInfo = true;
    //是否创建bean文件(默认是)
    private boolean needCreateBeanFile=true;
    //是否创建repository文件(默认是)
    private boolean needCreateRepositoryFile=true;
    //是否创建service文件(默认是)
    private boolean needCreateServiceFile=true;
    //是否创建controller文件(默认是)
    private boolean needCreateControllerFile=true;
    //是否需要创建bean时候加入字段验证注解
    private boolean needValidateBeanField=true;
    //是否需要加上controller save方法的验证注解
    private boolean needValidateSaveParam=true;

    public Config config;

    public static Helper newHelper(){
        return new Helper();
    }


    @Data
    public static class Helper{
        //是否需要创建信息(默认需要)
        private boolean needCreateInfo = true;
        //是否创建bean文件(默认是)
        private boolean needCreateBeanFile=true;
        //是否创建repository文件(默认是)
        private boolean needCreateRepositoryFile=true;
        //是否创建service文件(默认是)
        private boolean needCreateServiceFile=true;
        //是否创建controller文件(默认是)
        private boolean needCreateControllerFile=true;
        //是否需要创建bean时候加入字段验证注解
        private boolean needValidateBeanField=true;
        //是否需要加上controller save方法的验证注解
        private boolean needValidateSaveParam=true;

        //模块名(英文)
        private List<String> moduleName=new ArrayList<>();
        //模块名(中文)
        private List<String> moduleNameCN=new ArrayList<>();
        //表名
        private List<String> tableName=new ArrayList<>();

        private Helper(){

        }

        public Helper addModule(String moduleName,String moduleNameCN,String tableName){
            this.moduleName.add(moduleName);
            this.moduleNameCN.add(moduleNameCN);
            this.tableName.add(tableName);
            return this;
        }

        public List<TableConfig> toTableConfigs(){
            List<TableConfig> res=new ArrayList<>(this.moduleName.size());
            for (int i = 0; i < this.moduleName.size(); i++) {
                TableConfig tableConfig=new TableConfig();
                tableConfig.setModuleName(this.moduleName.get(i));
                tableConfig.setModuleNameCN(this.moduleNameCN.get(i));
                tableConfig.setTableName(this.tableName.get(i));
                tableConfig.setNeedCreateBeanFile(this.needCreateBeanFile);
                tableConfig.setNeedCreateRepositoryFile(this.needCreateRepositoryFile);
                tableConfig.setNeedCreateServiceFile(this.needCreateServiceFile);
                tableConfig.setNeedCreateControllerFile(this.needCreateControllerFile);
                tableConfig.setNeedCreateInfo(this.needCreateInfo);
                tableConfig.setNeedValidateBeanField(this.needValidateBeanField);
                tableConfig.setNeedValidateSaveParam(this.needValidateSaveParam);
                res.add(tableConfig);
            }
            return res;
        }

        public boolean isNeedCreateInfo() {
            return needCreateInfo;
        }

        public Helper setNeedCreateInfo(boolean needCreateInfo) {
            this.needCreateInfo = needCreateInfo;
            return this;
        }

        public boolean isNeedCreateBeanFile() {
            return needCreateBeanFile;
        }

        public Helper setNeedCreateBeanFile(boolean needCreateBeanFile) {
            this.needCreateBeanFile = needCreateBeanFile;
            return this;
        }

        public boolean isNeedCreateRepositoryFile() {
            return needCreateRepositoryFile;
        }

        public Helper setNeedCreateRepositoryFile(boolean needCreateRepositoryFile) {
            this.needCreateRepositoryFile = needCreateRepositoryFile;
            return this;
        }

        public boolean isNeedCreateServiceFile() {
            return needCreateServiceFile;
        }

        public Helper setNeedCreateServiceFile(boolean needCreateServiceFile) {
            this.needCreateServiceFile = needCreateServiceFile;
            return this;
        }

        public boolean isNeedCreateControllerFile() {
            return needCreateControllerFile;
        }

        public Helper setNeedCreateControllerFile(boolean needCreateControllerFile) {
            this.needCreateControllerFile = needCreateControllerFile;
            return this;
        }

        public boolean isNeedValidateBeanField() {
            return needValidateBeanField;
        }

        public Helper setNeedValidateBeanField(boolean needValidateBeanField) {
            this.needValidateBeanField = needValidateBeanField;
            return this;
        }

        public boolean isNeedValidateSaveParam() {
            return needValidateSaveParam;
        }

        public Helper setNeedValidateSaveParam(boolean needValidateSaveParam) {
            this.needValidateSaveParam = needValidateSaveParam;
            return this;
        }

    }

}