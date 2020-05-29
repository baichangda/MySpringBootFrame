package com.bcd.rdb.code.data;

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

    public static void main(String[] args) throws IOException, TemplateException {
        ControllerData controllerData=new ControllerData();
        controllerData.setPkType("Long");
        controllerData.setPackagePre("com.bcd.rdb.code.data");
        controllerData.setModuleName("Test");
        controllerData.setRequestMappingPre("test/test");
        controllerData.setModuleNameCN("测试");
        BeanField field1=new BeanField();
        field1.setName("id");
        field1.setType("Long");
        field1.setComment("主键(1,2)");
        field1.setNullable(false);
        BeanField field2=new BeanField();
        field2.setName("name");
        field2.setType("String");
        field2.setComment("姓名(张三)");
        field2.setNullable(true);
        field2.setStrLen(20);
        controllerData.setFieldList(Arrays.asList(field1,field2));


        Configuration configuration=new Configuration(Configuration.getVersion());
        configuration.setDirectoryForTemplateLoading(Paths.get("/Users/baichangda/bcd/workspace/MySpringBootFrame/RDB/src/main/java/com/bcd/rdb/code/freemarker/data").toFile());

        Template template=configuration.getTemplate("TemplateController.txt");

        Writer out=new FileWriter("/Users/baichangda/bcd/workspace/MySpringBootFrame/RDB/src/main/java/com/bcd/rdb/code/freemarker/data/controller/TestController.java");
        template.process(controllerData,out);
        out.flush();
        out.close();
    }
}
