package com.bcd.mongodb.code;

import com.bcd.base.define.CommonConst;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ClassUtil;
import com.bcd.base.util.FileUtil;
import com.bcd.mongodb.bean.BaseBean;
import com.bcd.mongodb.bean.SuperBaseBean;
import com.bcd.mongodb.test.bean.TestBean;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/10/10.
 */
@SuppressWarnings("unchecked")
public class CodeGenerator {

    private final static String CLASS_OUT_DIR_PATH ="out/production/classes";
    private final static String CLASS_BUILD_DIR_PATH ="build/classes/java/main";
    private final static String SOURCE_DIR_PATH="src/main/java";

    private final static Logger logger= LoggerFactory.getLogger(CodeGenerator.class);

    private final static String TEMPLATE_DIR_PATH = System.getProperty("user.dir") + "/MongoDB/src/main/resources/template";

    public static void initCollectionConfig(CollectionConfig collectionConfig){
        initBean(collectionConfig);
        initRepository(collectionConfig);
        initService(collectionConfig);
        initController(collectionConfig);
    }

    public static void initRepository(CollectionConfig collectionConfig){

    }

    public static void initService(CollectionConfig collectionConfig){

    }

    public static void initController(CollectionConfig collectionConfig){
        Map<String,Object> valueMap=collectionConfig.getValueMap();
        //是否需要controller save @Validated 注解(只有开启了bean注解并且需要param注解才开启)
        if(collectionConfig.isNeedParamValidate()){
            valueMap.put("paramValidateAnno","@Validated");
        }else{
            valueMap.put("paramValidateAnno","");
        }
        //缩进
        final String blank = "        ";
        List<JavaColumn> javaColumnList = (List<JavaColumn>) collectionConfig.getDataMap().get("fieldList");
        StringBuilder paramsSb = new StringBuilder();
        StringBuilder conditionsSb = new StringBuilder();
        for (int i = 0; i <= javaColumnList.size() - 1; i++) {
            JavaColumn column = javaColumnList.get(i);
            if(CodeConst.IGNORE_PARAM_NAME.contains(column.getName())){
                continue;
            }
            if (paramsSb.length()>0) {
                paramsSb.append(",");
                paramsSb.append("\n");
            }
            if(conditionsSb.length()>0){
                conditionsSb.append(",");
                conditionsSb.append("\n");
            }
            //取到原始类型对应的包装类型
            String type = CodeConst.BASE_TYPE_TO_PACKAGE_TYPE.getOrDefault(column.getType(),column.getType());
            String swaggerType=CodeConst.JAVA_TYPE_TO_SWAGGER_FULL_JAVA_TYPE.get(type);
            String param = column.getName();
            String paramBegin = column.getName() + "Begin";
            String paramEnd = column.getName() + "End";
            //1、controllerListParams
            paramsSb.append(blank);
            if (type.equals("Date")) {
                paramsSb.append("@ApiParam(value = \""+column.getComment()+"开始\")");
                paramsSb.append(" ");
                paramsSb.append("@RequestParam(value = \"" + paramBegin + "\", required = false) " + type + " " + paramBegin);
                paramsSb.append(",");
                paramsSb.append("\n");
                paramsSb.append(blank);
                paramsSb.append("@ApiParam(value = \""+column.getComment()+"结束\")");
                paramsSb.append(" ");
                paramsSb.append("@RequestParam(value = \"" + paramEnd + "\",required = false) " + type + " " + paramEnd);
            } else {
                paramsSb.append("@ApiParam(value = \""+column.getComment()+"\")");
                paramsSb.append(" ");
                paramsSb.append("@RequestParam(value = \"" + param + "\", required = false) " + type + " " + param);
            }
            //2、controllerListConditions
            String condition = CodeConst.TYPE_TO_CONDITION.get(column.getType());
            conditionsSb.append(blank);
            if (type.equals("Date")) {
                conditionsSb.append("    new " + condition + "(\"" + param + "\"," + paramBegin + ", " + condition + ".Handler.GE)");
                conditionsSb.append(",");
                conditionsSb.append("\n");
                conditionsSb.append(blank);
                conditionsSb.append("    new " + condition + "(\"" + param + "\"," + paramEnd + ", " + condition + ".Handler.LE)");
            }else if(type.equals("Boolean")||type.equals("boolean")) {
                conditionsSb.append("    new " + condition + "(\"" + param + "\"," + param + ")");
            }else if(type.equals("String")){
                conditionsSb.append("    new " + condition + "(\"" + param + "\"," + param + ", " + condition + ".Handler.ALL_LIKE)");
            }else{
                conditionsSb.append("    new " + condition + "(\"" + param + "\"," + param + ", " + condition + ".Handler.EQUAL)");
            }
        }
        collectionConfig.getValueMap().put("controllerListParams", paramsSb.toString());
        collectionConfig.getValueMap().put("controllerListConditions", conditionsSb.toString());
    }

    public static void initBean(CollectionConfig collectionConfig){
        Map<String,Object> dataMap= collectionConfig.getDataMap();
        Map<String,Object> valueMap=collectionConfig.getValueMap();
        //1、解析出大小写的模块名
        Class beanClass=collectionConfig.getClazz();
        String simpleBeanClassName=beanClass.getSimpleName();
        if(!simpleBeanClassName.endsWith("Bean")){
            throw BaseRuntimeException.getException("实体类格式必须为[*Bean],例如[TestBean]");
        }
        String upperModuleName=simpleBeanClassName.substring(0,simpleBeanClassName.length()-4);
        String lowerModuleName=upperModuleName.substring(0,1).toLowerCase()+upperModuleName.substring(1);
        dataMap.put("upperModuleName",upperModuleName);
        dataMap.put("lowerModuleName",lowerModuleName);
        valueMap.put("upperModuleName",upperModuleName);
        valueMap.put("lowerModuleName",lowerModuleName);
        //2、解析出当前beanClass对应java类的文件夹路径
        String classFilePath= beanClass.getResource("").getFile();
        String beanPath;
        if(classFilePath.contains(CLASS_OUT_DIR_PATH)){
            //替换out目录下
            beanPath=beanClass.getResource("").getFile().replace(CLASS_OUT_DIR_PATH,SOURCE_DIR_PATH);
        }else if(classFilePath.contains(CLASS_BUILD_DIR_PATH)){
            //替换build目录下
            beanPath=beanClass.getResource("").getFile().replace(CLASS_BUILD_DIR_PATH,SOURCE_DIR_PATH);
        }else{
            throw BaseRuntimeException.getException("initBean failed,class path["+classFilePath+"] not support");
        }
        String dirPath=Paths.get(beanPath).getParent().toString();
        dataMap.put("dirPath",dirPath);
        //3、解析出所有的字段和实体类的注释
        parseBeanClass(collectionConfig,beanClass);
        //4、解析包名
        initPackage(collectionConfig);
        //5、解析主键
        initPkType(collectionConfig);

    }

    private static void initPackage(CollectionConfig collectionConfig) {
        String springSrcPath = "src/main/java/";
        String formatDirPath = collectionConfig.getDataMap().get("dirPath").toString().replaceAll("\\\\", "/");
        if (formatDirPath.contains(springSrcPath)) {
            String pgk = formatDirPath.split(springSrcPath)[1].replaceAll("/", ".");
            collectionConfig.getValueMap().put("package", pgk);
            if(collectionConfig.getRequestMappingPre()==null){
                collectionConfig.getValueMap().put("requestMappingPre","/"+pgk.substring(pgk.lastIndexOf('.')+1));
            }
        }
    }

    private static Class getPKType(Class beanClass){
        Type parentType= ClassUtil.getParentUntil(beanClass,SuperBaseBean.class,BaseBean.class);
        return (Class) ((ParameterizedType) parentType).getActualTypeArguments()[0];
    }

    /**
     * 初始化主键类型
     * @param config
     * @throws Exception
     */
    private static void initPkType(CollectionConfig config){
        String pkType=getPKType(config.getClazz()).getTypeName();
        config.getValueMap().put("pkType",pkType.substring(pkType.lastIndexOf('.')+1));
    }

    /**
     * 通过读取class对应的java文件,获取
     * 1、实体类注释
     * 2、实体类字段集合
     * @param collectionConfig
     * @param beanClass
     */
    public static void parseBeanClass(CollectionConfig collectionConfig,Class beanClass){
        collectionConfig.getValueMap().put("moduleNameCN",collectionConfig.getModuleNameCN());
        List<Field> fieldList=FieldUtils.getAllFieldsList(beanClass).stream().filter(e->{
            if(e.getAnnotation(Transient.class)!=null){
                return false;
            }
            if("id".equals(e.getName())){
                return true;
            }
            for (Class aClass : CommonConst.BASE_DATA_TYPE) {
                if(aClass.isAssignableFrom(e.getType())){
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());


        Map<String,JavaColumn> columnMap= fieldList.stream().map(f-> {
            String fieldName = f.getName();
            Class fieldType;
            if ("id".equals(fieldName)) {
                fieldType = getPKType(beanClass);
            } else {
                fieldType = f.getType();
            }

            JavaColumn javaColumn= new JavaColumn(fieldName, fieldType.getSimpleName());
            ApiModelProperty apiModelProperty= f.getAnnotation(ApiModelProperty.class);
            if(apiModelProperty!=null){
                javaColumn.setComment(apiModelProperty.value());
            }
            return javaColumn;
        }).collect(Collectors.toMap(
                e->e.getName(),
                e->e,
                (e1,e2)->e1,
                ()->new LinkedHashMap<>()
        ));

        collectionConfig.getDataMap().put("fieldList",columnMap.values().stream().collect(Collectors.toList()));
    }

    /**
     * @param config
     */
    public static void generate(Config config) {
        CollectionConfig[] collectionConfigs = config.getCollectionConfigs();
        for (CollectionConfig collectionConfig : collectionConfigs) {
            try {
                initCollectionConfig(collectionConfig);
                Map<String,Object> dataMap=collectionConfig.getDataMap();
                String dirPath = dataMap.get("dirPath").toString();
                String upperModuleName=dataMap.get("upperModuleName").toString();
                Map<String,Object> valueMap = collectionConfig.getValueMap();
                Path templateDirPathObj = Paths.get(TEMPLATE_DIR_PATH);
                try {
                    Files.walkFileTree(templateDirPathObj, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            //1、先创建对应的文件
                            Path sub = file.subpath(templateDirPathObj.getNameCount(), file.getNameCount());
                            StringBuilder newPath = new StringBuilder();
                            newPath.append(dirPath);
                            newPath.append(File.separator);
                            newPath.append(sub.toString());
                            String newPathStr = newPath.toString().replace(".txt", ".java").replace("Template", upperModuleName);
                            Path newPathObj = Paths.get(newPathStr);
                            Files.deleteIfExists(newPathObj);
                            FileUtil.createNewFile(newPathObj);
                            //2、获取模版文件流@{template}
                            try (
                                    BufferedReader br = new BufferedReader(new FileReader(file.toFile()));
                                    PrintWriter pw = new PrintWriter(new FileWriter(newPathObj.toFile(), true))
                            ) {
                                String[] lineStr = new String[1];
                                while ((lineStr[0] = br.readLine()) != null) {
                                    valueMap.forEach((k, v) -> lineStr[0] = lineStr[0].replaceAll("@\\{" + k + "\\}", v.toString()));
                                    pw.println(lineStr[0]);
                                }
                                pw.flush();
                            }
                            logger.info("{} generate success!",newPathObj);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    logger.error("Error",e);
                }
            }catch (Exception e){
                logger.error("Error",e);
                return;
            }
        }
    }

    public static void main(String [] args){
        Config configProperties=new Config(
                new CollectionConfig("test","测试", TestBean.class)
        );
        CodeGenerator.generate(configProperties);
    }
}
