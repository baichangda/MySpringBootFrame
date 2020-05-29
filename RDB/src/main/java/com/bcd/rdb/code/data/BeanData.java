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

public class BeanData {

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
     * 父类
     * 1: #{@link com.bcd.rdb.bean.BaseBean}
     * 2: #{@link com.bcd.rdb.bean.SuperBaseBean}
     */
    private int superBeanType=2;

    /**
     * 主键类型
     */
    private String pkType;

    /**
     * 映射数据库表名
     */
    private String tableName;

    /**
     * 字段集合
     */
    private List<BeanField> fieldList;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPkType() {
        return pkType;
    }

    public void setPkType(String pkType) {
        this.pkType = pkType;
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

    public String getPackagePre() {
        return packagePre;
    }

    public void setPackagePre(String packagePre) {
        this.packagePre = packagePre;
    }

    public int getSuperBeanType() {
        return superBeanType;
    }

    public void setSuperBeanType(int superBeanType) {
        this.superBeanType = superBeanType;
    }

    public List<BeanField> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<BeanField> fieldList) {
        this.fieldList = fieldList;
    }



    public static void main(String[] args) throws IOException, TemplateException {
        BeanData beanData=new BeanData();
        beanData.setPkType("Long");
        beanData.setModuleNameCN("测试");
        beanData.setPackagePre("com.bcd.rdb.code.data");
        beanData.setTableName("t_test");
        beanData.setModuleName("Test");
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
        beanData.setFieldList(Arrays.asList(field1,field2));


        Configuration configuration=new Configuration(Configuration.getVersion());
        configuration.setDirectoryForTemplateLoading(Paths.get("/Users/baichangda/bcd/workspace/MySpringBootFrame/RDB/src/main/java/com/bcd/rdb/code/freemarker/data").toFile());

        Template template=configuration.getTemplate("TemplateBean.txt");

        Writer out=new FileWriter("/Users/baichangda/bcd/workspace/MySpringBootFrame/RDB/src/main/java/com/bcd/rdb/code/freemarker/data/bean/TestBean.java");
        template.process(beanData,out);
        out.flush();
        out.close();
    }
}

