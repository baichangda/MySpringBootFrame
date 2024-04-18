package com.bcd.base.support_jdbc.code;

import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.code.data.BeanData;
import com.bcd.base.support_jdbc.code.data.ControllerData;
import com.bcd.base.support_jdbc.code.data.ServiceData;
import com.bcd.base.support_jdbc.code.mysql.MysqlDBSupport;
import com.bcd.base.support_jdbc.code.pgsql.PgsqlDBSupport;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CodeGenerator {

    public final static CodeGenerator MYSQL = new CodeGenerator(new MysqlDBSupport());
    public final static CodeGenerator PGSQL = new CodeGenerator(new PgsqlDBSupport());
    static Logger logger = LoggerFactory.getLogger(CodeGenerator.class);
    final DBSupport dbSupport;

    public CodeGenerator(DBSupport dbSupport) {
        this.dbSupport = dbSupport;
    }

    public static void main(String[] args) {
        String path = "D:\\work\\bcd\\MySpringBootFrame\\src\\main\\java\\com\\bcd\\base\\support_jdbc\\code";
        final TableConfig.Helper helper = TableConfig.newHelper();
        helper.needCreateBeanFile = true;
        helper.needCreateServiceFile = true;
        helper.needCreateControllerFile = true;
        helper.needValidateBeanField = true;
        helper.needValidateSaveParam = true;
        helper
                .addModule("User", "用户", "t_sys_user")
                .addModule("Permission", "权限", "t_sys_permission");
        Config config = Config.newConfig(path).addTableConfig(helper.toTableConfigs());
        CodeGenerator.MYSQL.generate(config);
//        CodeGenerator.PGSQL.generate(config);
    }

    /**
     * 生成bean文件
     *
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateBean(BeanData data, String templateDir, String destDir) {
        Configuration configuration = new Configuration(CodeConst.FREEMARKER_VERSION);
        String fileDir = destDir + "/bean";
        try {
            Files.createDirectories(Paths.get(fileDir));
        } catch (IOException e) {
            throw MyException.get(e);
        }
        String destBeanPath = fileDir + "/" + data.moduleName.substring(0, 1).toUpperCase() + data.moduleName.substring(1) + "Bean.java";
        try (FileWriter out = new FileWriter(destBeanPath, StandardCharsets.UTF_8)) {
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("rdb_TemplateBean.txt", StandardCharsets.UTF_8.name());
            final DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper(CodeConst.FREEMARKER_VERSION);
            objectWrapper.setExposeFields(true);
            template.process(data, out, objectWrapper);
        } catch (IOException | TemplateException ex) {
            throw MyException.get(ex);
        }
        logger.info("{} generate succeed", destBeanPath);
    }

    /**
     * 生成service文件
     *
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateService(ServiceData data, String templateDir, String destDir) {
        Configuration configuration = new Configuration(CodeConst.FREEMARKER_VERSION);
        String fileDir = destDir + "/service";
        try {
            Files.createDirectories(Paths.get(fileDir));
        } catch (IOException e) {
            throw MyException.get(e);
        }
        String destBeanPath = fileDir + "/" + data.moduleName.substring(0, 1).toUpperCase() + data.moduleName.substring(1) + "Service.java";
        try (FileWriter out = new FileWriter(destBeanPath, StandardCharsets.UTF_8)) {
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("rdb_TemplateService.txt", StandardCharsets.UTF_8.name());
            final DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper(CodeConst.FREEMARKER_VERSION);
            objectWrapper.setExposeFields(true);
            template.process(data, out, objectWrapper);
        } catch (IOException | TemplateException ex) {
            throw MyException.get(ex);
        }
        logger.info("{} generate succeed", destBeanPath);
    }

    /**
     * 生成controller文件
     *
     * @param data
     * @param templateDir
     * @param destDir
     */
    public void generateController(ControllerData data, String templateDir, String destDir) {
        Configuration configuration = new Configuration(CodeConst.FREEMARKER_VERSION);
        String fileDir = destDir + "/controller";
        try {
            Files.createDirectories(Paths.get(fileDir));
        } catch (IOException e) {
            throw MyException.get(e);
        }
        String destBeanPath = fileDir + "/" + data.moduleName.substring(0, 1).toUpperCase() + data.moduleName.substring(1) + "Controller.java";
        try (FileWriter out = new FileWriter(destBeanPath, StandardCharsets.UTF_8)) {
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("rdb_TemplateController.txt", StandardCharsets.UTF_8.name());
            final DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper(CodeConst.FREEMARKER_VERSION);
            objectWrapper.setExposeFields(true);
            template.process(data, out, objectWrapper);
        } catch (IOException | TemplateException ex) {
            throw MyException.get(ex);
        }
        logger.info("{} generate succeed", destBeanPath);
    }

    /**
     * 根据配置和数据库信息初始化bean数据
     *
     * @param context
     * @return
     */
    public BeanData initBeanData(CodeGeneratorContext context) {
        BeanData data = new BeanData();
        data.moduleNameCN = context.tableConfig.moduleNameCN;
        data.moduleName = context.tableConfig.moduleName;
        data.packagePre = context.getPackagePre();
        data.tableName = context.tableConfig.tableName;
        data.containCreateAndUpdateField = context.getContainCreateAndUpdateField();
        data.fieldList = context.getDeclaredBeanFields();
        return data;
    }

    /**
     * 根据配置和数据库信息初始化service数据
     *
     * @param context
     * @return
     */
    public ServiceData initServiceData(CodeGeneratorContext context) {
        final TableConfig tableConfig = context.tableConfig;
        ServiceData data = new ServiceData();
        data.moduleNameCN = tableConfig.moduleNameCN;
        data.moduleName = tableConfig.moduleName;
        data.packagePre = context.getPackagePre();
        return data;
    }

    /**
     * 根据配置和数据库信息初始化controller数据
     *
     * @param context
     * @return
     */
    public ControllerData initControllerData(CodeGeneratorContext context) {
        final TableConfig tableConfig = context.tableConfig;
        ControllerData data = new ControllerData();
        data.moduleNameCN = tableConfig.moduleNameCN;
        data.moduleName = tableConfig.moduleName;
        data.packagePre = context.getPackagePre();
        data.fieldList = context.getAllBeanFields();
        data.validateSaveParam = tableConfig.needValidateSaveParam;
        data.requestMappingPre = context.getRequestMappingPre();
        return data;
    }

    /**
     * 初始化config属性
     * 1、数据库名称
     * 2、模版文件路径
     *
     * @param config
     */
    private void initConfig(Config config) {
        //如果配置了dbInfo、则不读取spring yml配置
        if (config.dbInfo == null) {
            config.dbInfo = dbSupport.getSpringDBConfig();
        }
        config.templateDirPath = Paths.get(config.templateDirPath == null ? CodeConst.TEMPLATE_DIR_PATH : config.templateDirPath).toString();
    }

    /**
     * 根据配置、application.yml数据库信息生成 bean/repository/service/controller 文件
     *
     * @param config
     */
    public void generate(Config config) {
        initConfig(config);
        try (Connection connection = getConnection(config.dbInfo.url, config.dbInfo.username, config.dbInfo.password)) {
            for (TableConfig tableConfig : config.tableConfigs) {
                CodeGeneratorContext context = new CodeGeneratorContext(tableConfig, dbSupport, connection);
                if (tableConfig.needCreateBeanFile) {
                    BeanData beanData = initBeanData(context);
                    generateBean(beanData, config.templateDirPath, config.targetDirPath);
                }
                if (tableConfig.needCreateServiceFile) {
                    ServiceData serviceData = initServiceData(context);
                    generateService(serviceData, config.templateDirPath, config.targetDirPath);
                }
                if (tableConfig.needCreateControllerFile) {
                    ControllerData controllerData = initControllerData(context);
                    generateController(controllerData, config.templateDirPath, config.targetDirPath);
                }
            }
        } catch (SQLException ex) {
            throw MyException.get(ex);
        }
    }

    public Connection getConnection(String url, String username, String password) {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw MyException.get(e);
        }
    }


}
