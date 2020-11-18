package com.bcd.rdb.code;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.FileUtil;
import com.bcd.rdb.code.data.*;
import com.bcd.rdb.code.mysql.MysqlDBSupport;
import com.bcd.rdb.code.pgsql.PgsqlDBSupport;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CodeGenerator {

    public final static CodeGenerator MYSQL =new CodeGenerator(new MysqlDBSupport());

    public final static CodeGenerator PGSQL =new CodeGenerator(new PgsqlDBSupport());

    DBSupport dbSupport;

    public CodeGenerator(DBSupport dbSupport){
        this.dbSupport=dbSupport;
    }

    static Logger logger= LoggerFactory.getLogger(CodeGenerator.class);
    /**
     * 生成bean文件
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateBean(BeanData data, String templateDir, String destDir){
        Configuration configuration=new Configuration(CodeConst.FREEMARKER_VERSION);
        String beanDir = destDir + "/bean";
        FileUtil.createDirectories(Paths.get(beanDir));
        String destBeanPath=beanDir + "/" + data.getModuleName().substring(0, 1).toUpperCase() + data.getModuleName().substring(1) + "Bean.java";
        try (FileWriter out = new FileWriter(destBeanPath)){
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("TemplateBean.txt");
            template.process(data, out);
        }catch (IOException | TemplateException ex){
            throw BaseRuntimeException.getException(ex);
        }
        logger.info("{} generate succeed",destBeanPath);
    }

    /**
     * 生成repository文件
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateRepository(RepositoryData data, String templateDir, String destDir){
        Configuration configuration=new Configuration(CodeConst.FREEMARKER_VERSION);
        String beanDir = destDir + "/repository";
        FileUtil.createDirectories(Paths.get(beanDir));
        String destBeanPath=beanDir + "/" + data.getModuleName().substring(0, 1).toUpperCase() + data.getModuleName().substring(1) + "Repository.java";
        try (FileWriter out = new FileWriter(destBeanPath)){
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("TemplateRepository.txt");
            template.process(data, out);
        }catch (IOException | TemplateException ex){
            throw BaseRuntimeException.getException(ex);
        }
        logger.info("{} generate succeed",destBeanPath);
    }

    /**
     * 生成service文件
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateService(ServiceData data, String templateDir, String destDir){
        Configuration configuration=new Configuration(CodeConst.FREEMARKER_VERSION);
        String beanDir = destDir + "/service";
        FileUtil.createDirectories(Paths.get(beanDir));
        String destBeanPath=beanDir + "/" + data.getModuleName().substring(0, 1).toUpperCase() + data.getModuleName().substring(1) + "Service.java";
        try (FileWriter out = new FileWriter(destBeanPath)){
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("TemplateService.txt");
            template.process(data, out);
        }catch (IOException | TemplateException ex){
            throw BaseRuntimeException.getException(ex);
        }
        logger.info("{} generate succeed",destBeanPath);
    }

    /**
     * 生成controller文件
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateController(ControllerData data, String templateDir, String destDir){
        Configuration configuration=new Configuration(CodeConst.FREEMARKER_VERSION);
        String beanDir = destDir + "/controller";
        FileUtil.createDirectories(Paths.get(beanDir));
        String destBeanPath=beanDir + "/" + data.getModuleName().substring(0, 1).toUpperCase() + data.getModuleName().substring(1) + "Controller.java";
        try (FileWriter out = new FileWriter(destBeanPath)){
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("TemplateController.txt");
            template.process(data, out);
        }catch (IOException | TemplateException ex){
            throw BaseRuntimeException.getException(ex);
        }
        logger.info("{} generate succeed",destBeanPath);
    }


    /**
     * 根据配置和数据库信息初始化bean数据
     * @param context
     * @return
     */
    public BeanData initBeanData(CodeGeneratorContext context){
        BeanData data=new BeanData();
        data.setModuleNameCN(context.getTableConfig().getModuleNameCN());
        data.setModuleName(context.getTableConfig().getModuleName());
        data.setPackagePre(context.getPackagePre());
        data.setTableName(context.getTableConfig().getTableName());
        data.setPkType(context.getPkType());
        data.setSuperBeanType(context.getTableConfig().isNeedCreateInfo()?1:2);
        data.setFieldList(context.getDeclaredBeanFields());
        return data;
    }

    /**
     * 根据配置和数据库信息初始化repository数据
     * @param context
     * @return
     */
    public RepositoryData initRepositoryData(CodeGeneratorContext context){
        RepositoryData data=new RepositoryData();
        data.setModuleNameCN(context.getTableConfig().getModuleNameCN());
        data.setModuleName(context.getTableConfig().getModuleName());
        data.setPackagePre(context.getPackagePre());
        data.setPkType(context.getPkType());
        return data;
    }

    /**
     * 根据配置和数据库信息初始化service数据
     * @param context
     * @return
     */
    public ServiceData initServiceData(CodeGeneratorContext context){
        ServiceData data=new ServiceData();
        data.setModuleNameCN(context.getTableConfig().getModuleNameCN());
        data.setModuleName(context.getTableConfig().getModuleName());
        data.setPackagePre(context.getPackagePre());
        data.setPkType(context.getPkType());
        return data;
    }

    /**
     * 根据配置和数据库信息初始化controller数据
     * @param context
     * @return
     */
    public ControllerData initControllerData(CodeGeneratorContext context){
        ControllerData data=new ControllerData();
        data.setModuleNameCN(context.getTableConfig().getModuleNameCN());
        data.setModuleName(context.getTableConfig().getModuleName());
        data.setPackagePre(context.getPackagePre());
        data.setPkType(context.getPkType());
        data.setFieldList(context.getAllBeanFields());
        data.setValidateSaveParam(context.getTableConfig().isNeedValidateSaveParam());
        data.setRequestMappingPre(context.getRequestMappingPre());
        return data;
    }


    /**
     * 初始化config属性
     * 1、数据库名称
     * 2、模版文件路径
     * @param config
     */
    private void initConfig(Config config){
        config.setDb(dbSupport.getDb());
        config.setTemplateDirPath(Paths.get(config.getTemplateDirPath()==null? CodeConst.TEMPLATE_DIR_PATH:config.getTemplateDirPath()).toString());
    }

    /**
     * 根据配置、application.yml数据库信息生成 bean/repository/service/controller 文件
     * @param config
     */
    public void generate(Config config){
        initConfig(config);
        try(Connection connection=dbSupport.getSpringConn()){
            for (TableConfig tableConfig : config.getTableConfigs()) {
                CodeGeneratorContext context=new CodeGeneratorContext(tableConfig,dbSupport,connection);
                if(tableConfig.isNeedCreateBeanFile()){
                    BeanData beanData= initBeanData(context);
                    generateBean(beanData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateRepositoryFile()) {
                    RepositoryData repositoryData = initRepositoryData(context);
                    generateRepository(repositoryData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateServiceFile()) {
                    ServiceData serviceData = initServiceData(context);
                    generateService(serviceData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateControllerFile()) {
                    ControllerData controllerData = initControllerData(context);
                    generateController(controllerData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
            }
        }catch (SQLException ex){
            throw BaseRuntimeException.getException(ex);
        }
    }


    public static void main(String[] args) {
//        String path = "/Users/baichangda/bcd/workspace/MySpringBootFrame/RDB/src/main/java/com/bcd/rdb/code";
        String path = "D:\\workspace\\MySpringBootFrame\\RDB\\src\\main\\java\\com\\bcd\\rdb\\code";
        List<TableConfig> tableConfigs= TableConfig.newHelper()
                .setNeedCreateBeanFile(true)
                .setNeedCreateRepositoryFile(true)
                .setNeedCreateServiceFile(true)
                .setNeedCreateControllerFile(true)
                .setNeedValidateBeanField(true)
                .setNeedValidateSaveParam(true)
                .setNeedCreateInfo(true)
                .addModule("User", "用户", "t_sys_user")
                .addModule("Permission", "权限", "t_sys_permission")
                .toTableConfigs();
        Config config= Config.newConfig(path).addTableConfig(tableConfigs);
//        CodeGenerator.MYSQL.generate(config);
        CodeGenerator.PGSQL.generate(config);
    }

}
