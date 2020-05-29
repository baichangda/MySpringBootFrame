package com.bcd.mongodb.code.freemarker;

import com.bcd.base.define.CommonConst;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.FileUtil;
import com.bcd.mongodb.bean.BaseBean;
import com.bcd.mongodb.bean.SuperBaseBean;
import com.bcd.mongodb.code.freemarker.data.*;
import com.bcd.mongodb.test.bean.TestBean;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGenerator {

    static Logger logger=LoggerFactory.getLogger(CodeGenerator.class);

    /**
     * 生成repository文件
     * @param data
     * @param templateDir
     * @param destDir
     */
    public static void generateRepository(RepositoryData data, String templateDir, String destDir){
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
    public static void generateService(ServiceData data, String templateDir, String destDir){
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
    public static void generateController(ControllerData data, String templateDir, String destDir){
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
     * 根据配置初始化repository数据
     * @param config
     * @return
     */
    public static RepositoryData initRepositoryData(CollectionConfig config){
        RepositoryData data=new RepositoryData();
        data.setModuleNameCN(config.getModuleNameCN());
        data.setModuleName(config.getModuleName());
        data.setPackagePre(initPackagePre(config));
        data.setPkType(initPkType(config));
        return data;
    }

    /**
     * 根据配置初始化service数据
     * @param config
     * @return
     */
    public static ServiceData initServiceData(CollectionConfig config){
        ServiceData data=new ServiceData();
        data.setModuleNameCN(config.getModuleNameCN());
        data.setModuleName(config.getModuleName());
        data.setPackagePre(initPackagePre(config));
        data.setPkType(initPkType(config));
        return data;
    }

    /**
     * 根据配置初始化controller数据
     * @param config
     * @return
     */
    public static ControllerData initControllerData(CollectionConfig config){
        ControllerData data=new ControllerData();
        data.setModuleNameCN(config.getModuleNameCN());
        data.setModuleName(config.getModuleName());
        data.setPackagePre(initPackagePre(config));
        data.setPkType(initPkType(config));
        data.setFieldList(initBeanField(config));
        data.setValidateSaveParam(config.isNeedValidateSaveParam());
        data.setRequestMappingPre(initRequestMappingPre(data.getPackagePre()));
        return data;
    }

    /**
     * 初始化主键类型
     * @param config
     * @throws Exception
     */
    private static String initPkType(CollectionConfig config){
        return getPKType(config.getClazz()).getSimpleName();
    }

    private static Class getPKType(Class beanClass){
        Type parentType= ClassUtil.getParentUntil(beanClass, SuperBaseBean.class, BaseBean.class);
        return (Class) ((ParameterizedType) parentType).getActualTypeArguments()[0];
    }

    /**
     * 通过读取class对应的java文件,获取
     * 1、实体类注释
     * 2、实体类字段集合
     * @param config
     */
    public static List<BeanField> initBeanField(CollectionConfig config){
        List<Field> fieldList=FieldUtils.getAllFieldsList(config.getClazz()).stream().filter(e->{
            if(e.getAnnotation(Transient.class)!=null){
                return false;
            }
            if("id".equals(e.getName())){
                return true;
            }
            for (Class<?> aClass : CommonConst.BASE_DATA_TYPE) {
                if(aClass.isAssignableFrom(e.getType())){
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());


        Map<String, BeanField> beanFieldMap= fieldList.stream().map(f-> {
            String fieldName = f.getName();
            Class fieldType;
            if ("id".equals(fieldName)) {
                fieldType = getPKType(config.getClazz());
            } else {
                fieldType = f.getType();
            }

            BeanField beanField= new BeanField();
            beanField.setName(fieldName);
            beanField.setType(fieldType.getSimpleName());
            ApiModelProperty apiModelProperty= f.getAnnotation(ApiModelProperty.class);
            if(apiModelProperty!=null){
                beanField.setComment(apiModelProperty.value());
            }
            return beanField;
        }).collect(Collectors.toMap(
                e->e.getName(),
                e->e,
                (e1,e2)->e1,
                ()->new LinkedHashMap<>()
        ));

        return new ArrayList<>(beanFieldMap.values());
    }



    /**
     * 初始化包名
     * 初始化当前表生成代码目录父包名
     * @param config
     */
    private static String initPackagePre(CollectionConfig config) {
        StringBuilder springSrcPathSb=new StringBuilder();
        springSrcPathSb.append("src");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("main");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("java");
        springSrcPathSb.append(File.separatorChar);
        String springSrcPath = springSrcPathSb.toString();
        String targetDirPath=config.getTargetDirPath();
        if (targetDirPath.contains(springSrcPath)) {
            return targetDirPath.split(springSrcPath)[1].replaceAll(File.separator, ".");
        }else{
            throw BaseRuntimeException.getException("targetDirPath["+targetDirPath+"] must contains ["+springSrcPath+"]");
        }
    }

    /**
     * 初始化request mapping
     * @param packagePre
     * @return
     */
    private static String initRequestMappingPre(String packagePre){
        return "/"+packagePre.substring(packagePre.lastIndexOf('.')+1);
    }

    /**
     * 初始化config属性
     * 1、数据库名称
     * 2、模版文件路径
     * @param config
     */
    private static void initConfig(CollectionConfig config){
        config.setTemplateDirPath(Paths.get(config.getTemplateDirPath()==null? CodeConst.TEMPLATE_DIR_PATH:config.getTemplateDirPath()).toString());
    }

    /**
     * 根据配置、application.yml数据库信息生成 bean/repository/service/controller 文件
     * @param config
     */
    public static void generate(CollectionConfig config){
        initConfig(config);
        if(config.isNeedCreateRepositoryFile()) {
            RepositoryData repositoryData = initRepositoryData(config);
            generateRepository(repositoryData,config.getTemplateDirPath(),config.getTargetDirPath());
        }
        if(config.isNeedCreateServiceFile()) {
            ServiceData serviceData = initServiceData(config);
            generateService(serviceData,config.getTemplateDirPath(),config.getTargetDirPath());
        }
        if(config.isNeedCreateControllerFile()) {
            ControllerData controllerData = initControllerData(config);
            generateController(controllerData,config.getTemplateDirPath(),config.getTargetDirPath());
        }
    }

    public static void main(String[] args) {
        CollectionConfig config = new CollectionConfig("Test", "测试", TestBean.class)
                                .setNeedCreateControllerFile(true)
                                .setNeedCreateServiceFile(true)
                                .setNeedCreateRepositoryFile(true)
                                .setNeedValidateSaveParam(true);
        CodeGenerator.generate(config);
    }

}
