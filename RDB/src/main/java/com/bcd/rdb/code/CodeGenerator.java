package com.bcd.rdb.code;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.FileUtil;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.code.data.*;
import com.bcd.rdb.code.mysql.MysqlDBSupport;
import com.bcd.rdb.code.pgsql.PgsqlDBSupport;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
     * @param tableConfig
     * @param connection
     * @return
     */
    public BeanData initBeanData(TableConfig tableConfig,Connection connection){
        BeanData data=new BeanData();
        data.setModuleNameCN(tableConfig.getModuleNameCN());
        data.setModuleName(tableConfig.getModuleName());
        data.setPackagePre(initPackagePre(tableConfig));
        data.setTableName(tableConfig.getTableName());
        data.setPkType(initPkType(connection,tableConfig));
        data.setSuperBeanType(tableConfig.isNeedCreateInfo()?1:2);
        data.setFieldList(initBeanField(tableConfig,connection));
        return data;
    }

    /**
     * 根据配置和数据库信息初始化repository数据
     * @param tableConfig
     * @param connection
     * @return
     */
    public RepositoryData initRepositoryData(TableConfig tableConfig,Connection connection){
        RepositoryData data=new RepositoryData();
        data.setModuleNameCN(tableConfig.getModuleNameCN());
        data.setModuleName(tableConfig.getModuleName());
        data.setPackagePre(initPackagePre(tableConfig));
        data.setPkType(initPkType(connection,tableConfig));
        return data;
    }

    /**
     * 根据配置和数据库信息初始化service数据
     * @param tableConfig
     * @param connection
     * @return
     */
    public ServiceData initServiceData(TableConfig tableConfig,Connection connection){
        ServiceData data=new ServiceData();
        data.setModuleNameCN(tableConfig.getModuleNameCN());
        data.setModuleName(tableConfig.getModuleName());
        data.setPackagePre(initPackagePre(tableConfig));
        data.setPkType(initPkType(connection,tableConfig));
        return data;
    }

    /**
     * 根据配置和数据库信息初始化controller数据
     * @param tableConfig
     * @param connection
     * @return
     */
    public ControllerData initControllerData(TableConfig tableConfig,Connection connection){
        ControllerData data=new ControllerData();
        data.setModuleNameCN(tableConfig.getModuleNameCN());
        data.setModuleName(tableConfig.getModuleName());
        data.setPackagePre(initPackagePre(tableConfig));
        data.setPkType(initPkType(connection,tableConfig));
        data.setFieldList(initBeanField(tableConfig,connection));
        data.setValidateSaveParam(tableConfig.isNeedValidateSaveParam());
        data.setRequestMappingPre(initRequestMappingPre(data.getPackagePre()));
        return data;
    }


    /**
     * 初始化主键类型
     * @param connection
     * @param config
     */
    private String initPkType(Connection connection, TableConfig config){
        return dbSupport.getTablePkType(config,connection).toString();
    }

    /**
     * 初始化java字段集合
     * @param config
     * @param connection
     */
    private List<BeanField> initBeanField(TableConfig config,Connection connection){
        return dbSupport.getTableBeanFieldList(config,connection);
    }

    /**
     * 初始化包名
     * 初始化当前表生成代码目录父包名
     * @param config
     */
    private String initPackagePre(TableConfig config) {
        StringBuilder springSrcPathSb=new StringBuilder();
        springSrcPathSb.append("src");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("main");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("java");
        springSrcPathSb.append(File.separatorChar);
        String springSrcPath = springSrcPathSb.toString();
        String targetDirPath=config.getConfig().getTargetDirPath();
        if (targetDirPath.contains(springSrcPath)) {
            return targetDirPath.split(StringUtil.escapeExprSpecialWord(springSrcPath))[1].replaceAll(StringUtil.escapeExprSpecialWord(File.separator), ".");
        }else{
            throw BaseRuntimeException.getException("targetDirPath["+targetDirPath+"] must contains ["+springSrcPath+"]");
        }
    }

    /**
     * 初始化request mapping
     * @param packagePre
     * @return
     */
    private String initRequestMappingPre(String packagePre){
        return "/"+packagePre.substring(packagePre.lastIndexOf('.')+1);
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
                if(tableConfig.isNeedCreateBeanFile()){
                    BeanData beanData= initBeanData(tableConfig,connection);
                    generateBean(beanData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateRepositoryFile()) {
                    RepositoryData repositoryData = initRepositoryData(tableConfig, connection);
                    generateRepository(repositoryData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateServiceFile()) {
                    ServiceData serviceData = initServiceData(tableConfig, connection);
                    generateService(serviceData,config.getTemplateDirPath(),config.getTargetDirPath());
                }
                if(tableConfig.isNeedCreateControllerFile()) {
                    ControllerData controllerData = initControllerData(tableConfig, connection);
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
