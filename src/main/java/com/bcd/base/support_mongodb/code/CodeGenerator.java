package com.bcd.base.support_mongodb.code;

import com.bcd.base.exception.BaseException;
import com.bcd.base.support_mongodb.bean.SuperBaseBean;
import com.bcd.base.support_mongodb.code.data.*;
import com.bcd.base.util.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

    /**
     * 生成service文件
     *
     * @param data
     * @param templateDir
     * @param destDir
     */
    public static void generateService(ServiceData data, String templateDir, String destDir) {
        Configuration configuration = new Configuration(CodeConst.FREEMARKER_VERSION);
        String fileDir = destDir + "/service";
        try {
            Files.createDirectories(Paths.get(fileDir));
        } catch (IOException e) {
            throw BaseException.get(e);
        }
        String destBeanPath = fileDir + "/" + data.moduleName.substring(0, 1).toUpperCase() + data.moduleName.substring(1) + "Service.java";
        try (FileWriter out = new FileWriter(destBeanPath, StandardCharsets.UTF_8)) {
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("mongo_TemplateService.txt", StandardCharsets.UTF_8.name());
            final DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper(CodeConst.FREEMARKER_VERSION);
            objectWrapper.setExposeFields(true);
            template.process(data, out, objectWrapper);
        } catch (IOException | TemplateException ex) {
            throw BaseException.get(ex);
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
    public static void generateController(ControllerData data, String templateDir, String destDir) {
        Configuration configuration = new Configuration(CodeConst.FREEMARKER_VERSION);
        String fileDir = destDir + "/controller";
        try {
            Files.createDirectories(Paths.get(fileDir));
        } catch (IOException e) {
            throw BaseException.get(e);
        }
        String destBeanPath = fileDir + "/" + data.moduleName.substring(0, 1).toUpperCase() + data.moduleName.substring(1) + "Controller.java";
        try (FileWriter out = new FileWriter(destBeanPath, StandardCharsets.UTF_8)) {
            configuration.setDirectoryForTemplateLoading(Paths.get(templateDir).toFile());
            Template template = configuration.getTemplate("mongo_TemplateController.txt", StandardCharsets.UTF_8.name());
            final DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper(CodeConst.FREEMARKER_VERSION);
            objectWrapper.setExposeFields(true);
            template.process(data, out, objectWrapper);
        } catch (IOException | TemplateException ex) {
            throw BaseException.get(ex);
        }
        logger.info("{} generate succeed", destBeanPath);
    }


    /**
     * 根据配置初始化service数据
     *
     * @param config
     * @return
     */
    public static ServiceData initServiceData(CollectionConfig config) {
        ServiceData data = new ServiceData();
        data.moduleNameCN = config.moduleNameCN;
        data.moduleName = config.moduleName;
        data.packagePre = initPackagePre(config);
        return data;
    }

    /**
     * 根据配置初始化controller数据
     *
     * @param config
     * @return
     */
    public static ControllerData initControllerData(CollectionConfig config) {
        ControllerData data = new ControllerData();
        data.moduleNameCN = config.moduleNameCN;
        data.moduleName = config.moduleName;
        data.packagePre = initPackagePre(config);
        data.fieldList = initBeanField(config);
        data.validateSaveParam = config.needValidateSaveParam;
        data.requestMappingPre = initRequestMappingPre(data.packagePre);
        return data;
    }

    /**
     * 通过读取class对应的java文件,获取
     * 1、实体类注释
     * 2、实体类字段集合
     *
     * @param config
     */
    public static List<BeanField> initBeanField(CollectionConfig config) {
        if (!SuperBaseBean.class.isAssignableFrom(config.clazz)) {
            throw BaseException.get("bean[{}] must extends SuperBaseBean", config.clazz.getName());
        }
        List<Field> fieldList = FieldUtils.getAllFieldsList(config.clazz).stream().filter(e -> {
            if (e.getAnnotation(Transient.class) != null) {
                return false;
            }
            if (Modifier.isStatic(e.getModifiers())) {
                return false;
            }
            for (Class<?> aClass : CodeConst.SUPPORT_FIELD_TYPE) {
                if (aClass.isAssignableFrom(e.getType())) {
                    return true;
                }
            }
            return false;
        }).toList();


        Map<String, BeanField> beanFieldMap = fieldList.stream().map(f -> {
            String fieldName = f.getName();
            Class<?> fieldType = f.getType();
            BeanField beanField = new BeanField();
            beanField.name = fieldName;
            beanField.type = fieldType.getSimpleName();
            Schema schema = f.getAnnotation(Schema.class);
            if (schema != null) {
                beanField.setComment(schema.description());
            }
            return beanField;
        }).collect(Collectors.toMap(
                e -> e.name,
                e -> e,
                (e1, e2) -> e1,
                LinkedHashMap::new
        ));

        return new ArrayList<>(beanFieldMap.values());
    }


    /**
     * 初始化包名
     * 初始化当前表生成代码目录父包名
     *
     * @param config
     */
    private static String initPackagePre(CollectionConfig config) {
        StringBuilder springSrcPathSb = new StringBuilder();
        springSrcPathSb.append("src");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("main");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("java");
        springSrcPathSb.append(File.separatorChar);
        String springSrcPath = springSrcPathSb.toString();
        String targetDirPath = config.targetDirPath;
        if (targetDirPath.contains(springSrcPath)) {
            return targetDirPath.split(StringUtil.escapeExprSpecialWord(springSrcPath))[1].replaceAll(StringUtil.escapeExprSpecialWord(File.separator), ".");
        } else {
            throw BaseException.get("targetDirPath[" + targetDirPath + "] must contains [" + springSrcPath + "]");
        }
    }

    /**
     * 初始化request mapping
     *
     * @param packagePre
     * @return
     */
    private static String initRequestMappingPre(String packagePre) {
        return "/" + packagePre.substring(packagePre.lastIndexOf('.') + 1);
    }

    /**
     * 初始化config属性
     * 1、数据库名称
     * 2、模版文件路径
     *
     * @param config
     */
    private static void initConfig(CollectionConfig config) {
        config.templateDirPath = Paths.get(config.templateDirPath == null ? CodeConst.TEMPLATE_DIR_PATH : config.templateDirPath).toString();
    }

    /**
     * 根据配置、application.yml数据库信息生成 bean/repository/service/controller 文件
     *
     * @param config
     */
    public static void generate(CollectionConfig config) {
        initConfig(config);
        if (config.needCreateServiceFile) {
            ServiceData serviceData = initServiceData(config);
            generateService(serviceData, config.templateDirPath, config.targetDirPath);
        }
        if (config.needCreateControllerFile) {
            ControllerData controllerData = initControllerData(config);
            generateController(controllerData, config.templateDirPath, config.targetDirPath);
        }
    }


}
