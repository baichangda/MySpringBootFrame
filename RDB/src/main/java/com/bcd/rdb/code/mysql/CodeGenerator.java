package com.bcd.rdb.code.mysql;


import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.FileUtil;
import com.bcd.rdb.code.Config;
import com.bcd.rdb.code.TableConfig;
import com.bcd.rdb.dbinfo.mysql.util.DBInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/28.
 */
@SuppressWarnings("unchecked")
public class CodeGenerator {
    private final static Logger logger= LoggerFactory.getLogger(CodeGenerator.class);
    public static void generate(List<Config> configList) {
        configList.forEach(config -> generate(config));
    }

    /**
     * 需要如下参数
     *
     * @param config
     */
    private static void generate(Config config) {
        for (TableConfig tableConfig : config.getTableConfigs())
            try {
                initTableConfig(tableConfig);
                String dirPath = config.getTargetDirPath();
                Map<String,Object> valueMap = tableConfig.getValueMap();
                Path templateDirPathObj = Paths.get(config.getTemplateDirPath()==null?CodeConst.TEMPLATE_DIR_PATH:config.getTemplateDirPath());
                try {
                    Files.walkFileTree(templateDirPathObj, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            //pre:根据配置决定是否跳过当前文件的生成
                            String fileName=file.getFileName().toString();
                            switch (fileName){
                                case "TemplateBean.txt":{
                                    if(!tableConfig.needCreateBeanFile){
                                        return FileVisitResult.CONTINUE;
                                    }else{
                                        break;
                                    }
                                }
                                case "TemplateRepository.txt":{
                                    if(!tableConfig.needCreateRepositoryFile){
                                        return FileVisitResult.CONTINUE;
                                    }else{
                                        break;
                                    }
                                }
                                case "TemplateService.txt":{
                                    if(!tableConfig.needCreateServiceFile){
                                        return FileVisitResult.CONTINUE;
                                    }else{
                                        break;
                                    }
                                }
                                case "TemplateController.txt":{
                                    if(!tableConfig.needCreateControllerFile){
                                        return FileVisitResult.CONTINUE;
                                    }else{
                                        break;
                                    }
                                }
                                default:{
                                    return FileVisitResult.CONTINUE;
                                }
                            }
                            //1、先创建对应的文件
                            Path sub = file.subpath(templateDirPathObj.getNameCount(), file.getNameCount());
                            StringBuilder newPath = new StringBuilder();
                            newPath.append(dirPath);
                            newPath.append(File.separator);
                            newPath.append(sub.toString());
                            String newPathStr = newPath.toString().replace(".txt", ".java").replace("Template", tableConfig.getModuleName());
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
                            System.out.println(newPathObj.toString() + " generate success!");
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

            } catch (Exception e) {
                logger.error("Error",e);
                return;
            }
    }

    /**
     * 初始化模块名大小写
     *
     * @param config
     */
    private static void initTableConfig(TableConfig config) throws Exception {
        //初始化简单属性
        initSimpleProp(config);
        //初始化模块注释
        initModuleDesc(config);
        //初始化主键类型
        initPkType(config);
        //初始化java字段集合
        initJavaField(config);
        //初始化Bean模版需要的属性和方法
        initBeanConfig(config);
        //初始化Controller模版需要的swagger注解和参数
        initControllerConfig(config);
        //解析形成包名
        initPackage(config);

    }

    /**
     * 初始化主键类型
     * @param config
     * @throws Exception
     */
    private static void initPkType(TableConfig config)throws Exception {
        Map<String,Object> pk= DBInfoUtil.findPKColumn(config.getDataMap().get("dbName").toString(),config.getTableName());
        String pk_db_type=pk.get("DATA_TYPE").toString();
        String pk_java_type=CodeConst.DB_TYPE_TO_JAVA_TYPE.get(pk_db_type);
        config.getValueMap().put("pkType",pk_java_type);
    }

    /**
     * 初始化简单属性
     * @param config
     */
    private static void initSimpleProp(TableConfig config)throws Exception {
        String moduleName = config.getModuleName();
        Character firstChar = moduleName.charAt(0);
        Map<String,Object> valueMap= config.getValueMap();
        Map<String,Object> dbProps=  DBInfoUtil.getDBProps();
        config.getDataMap().putAll(dbProps);
        //大写模块名
        valueMap.put("upperModuleName", Character.toUpperCase(firstChar) + moduleName.substring(1));
        //小写模块名
        valueMap.put("lowerModuleName", Character.toLowerCase(firstChar) + moduleName.substring(1));
        //模块名中文
        valueMap.put("moduleNameCN",config.getModuleNameCN());
        //表名
        valueMap.put("tableName",config.getTableName());
        //是否需要controller save @Validated 注解(只有开启了bean注解并且需要param注解才开启)
        if(config.isNeedBeanValidate()&&config.isNeedParamValidate()){
            valueMap.put("paramValidateAnno","@Validated");
        }else{
            valueMap.put("paramValidateAnno","");
        }
        //初始化bean父类和忽视的属性set
        Set<String> ignoreColumnSet = new HashSet<>();
        ignoreColumnSet.add("id");
        if (config.isNeedCreateInfo()) {
            config.getValueMap().put("superBean", "BaseBean");
            ignoreColumnSet.addAll(CodeConst.IGNORE_FIELD_NAME);
        } else {
            config.getValueMap().put("superBean", "SuperBaseBean");
        }
        config.getDataMap().put("ignoreColumnSet", ignoreColumnSet);
    }

    private static void initModuleDesc(TableConfig config) throws Exception{
        String tableName = config.getTableName();
        String dbName=config.getDataMap().get("dbName").toString();
        Map<String,Object> res= DBInfoUtil.findTable(dbName,tableName);
        if(res==null){
            throw BaseRuntimeException.getException("数据库["+dbName+"]不存在表["+tableName+"]");
        }
        config.getValueMap().put("moduleDesc",res.get("TABLE_COMMENT"));
    }

    /**
     * 初始化java字段集合
     * @param config
     */
    private static void initJavaField(TableConfig config) throws Exception {
        String tableName = config.getTableName();
        List<Map<String,Object>> res = DBInfoUtil.findColumns(config.getDataMap().get("dbName").toString(),tableName);
        List<JavaColumn> javaColumnList = res.stream().map(e -> {
            DBColumn dbColumn = new DBColumn();
            dbColumn.setName(e.get("COLUMN_NAME").toString());
            dbColumn.setType(e.get("DATA_TYPE").toString());
            dbColumn.setComment(e.get("COLUMN_COMMENT").toString());
            dbColumn.setIsNull(e.get("IS_NULLABLE").toString());
            dbColumn.setStrLen(Integer.parseInt(e.get("CHARACTER_MAXIMUM_LENGTH").toString()));
            JavaColumn javaColumn=dbColumn.toJavaColumn();
            return javaColumn;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        config.getDataMap().put("fieldList", javaColumnList);
    }

    /**
     * 初始化包名
     * 初始化当前表生成代码目录父包名
     * @param config
     */
    private static void initPackage(TableConfig config) {
        StringBuilder springSrcPathSb=new StringBuilder();
        springSrcPathSb.append("src");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("main");
        springSrcPathSb.append(File.separatorChar);
        springSrcPathSb.append("java");
        springSrcPathSb.append(File.separatorChar);
        String springSrcPath = springSrcPathSb.toString();
        String descDirPath=config.getConfig().getTargetDirPath();
        if (descDirPath.contains(springSrcPath)) {
            String pgk = descDirPath.split(springSrcPath)[1].replaceAll(File.separator, ".");
            config.getValueMap().put("package", pgk);
            if(config.getRequestMappingPre()==null){
                config.getValueMap().put("requestMappingPre","/"+pgk.substring(pgk.lastIndexOf('.')+1));
            }
        }
    }

    /**
     * 初始化controller
     *
     * @param config
     */
    private static void initControllerConfig(TableConfig config) {
        //缩进
        final String blank = "            ";
        List<JavaColumn> javaColumnList = (List<JavaColumn>) config.getDataMap().get("fieldList");
        StringBuilder paramsSb = new StringBuilder();
        StringBuilder conditionsSb = new StringBuilder();
        for (int i = 0; i <= javaColumnList.size() - 1; i++) {
            JavaColumn column = javaColumnList.get(i);
            if(CodeConst.IGNORE_FIELD_NAME.contains(column.getName())){
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
            String swaggerExample= CodeConst.PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.get(type);
            String param = column.getName();
            String paramBegin = column.getName() + "Begin";
            String paramEnd = column.getName() + "End";
            //1、controllerListSwaggerParams和controllerListParams
            paramsSb.append(blank);
            if (type.equals("Date")) {
                paramsSb.append("@ApiParam(value = \""+column.getComment()+"开始\")");
                paramsSb.append("\n");
                paramsSb.append(blank);
                paramsSb.append("@RequestParam(value = \"" + paramBegin + "\",required = false) " + type + " " + paramBegin);
                paramsSb.append(",");
                paramsSb.append("\n");
                paramsSb.append(blank);
                paramsSb.append("@ApiParam(value = \""+column.getComment()+"截止\")");
                paramsSb.append("\n");
                paramsSb.append(blank);
                paramsSb.append("@RequestParam(value = \"" + paramEnd + "\",required = false) " + type + " " + paramEnd);
            } else {
                if(swaggerExample==null){
                    paramsSb.append("@ApiParam(value = \""+column.getComment()+"\")");
                    paramsSb.append("\n");
                    paramsSb.append(blank);
                }else{
                    paramsSb.append("@ApiParam(value = \""+column.getComment()+"\",example=\""+swaggerExample+"\")");
                    paramsSb.append("\n");
                    paramsSb.append(blank);
                }
                paramsSb.append("@RequestParam(value = \"" + param + "\",required = false) " + type + " " + param);
            }
            //2、controllerListConditions
            String condition = CodeConst.TYPE_TO_CONDITION.get(column.getType());
            conditionsSb.append(blank);
            if (type.equals("Date")) {
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + paramBegin + ", " + condition + ".Handler.GE)");
                conditionsSb.append(",");
                conditionsSb.append("\n");
                conditionsSb.append(blank);
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + paramEnd + ", " + condition + ".Handler.LE)");
            }else if(type.equals("Boolean")||type.equals("boolean")) {
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + param + ")");
            }else if(type.equals("String")){
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + param + ", " + condition + ".Handler.ALL_LIKE)");
            }else{
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + param + ", " + condition + ".Handler.EQUAL)");
            }

        }
        config.getValueMap().put("controllerListParams", paramsSb.toString());
        config.getValueMap().put("controllerListConditions", conditionsSb.toString());
    }



    /**
     * 初始化Bean的配置
     *
     * @param config
     * @return
     */
    private static void initBeanConfig(TableConfig config) {
        Set<String> ignoreColumnSet = (Set<String>) config.getDataMap().get("ignoreColumnSet");
        String blank = "    ";
        List<JavaColumn> javaColumnList = (List<JavaColumn>) config.getDataMap().get("fieldList");
        StringBuilder fieldSb = new StringBuilder();
        StringBuilder methodSb = new StringBuilder();
        for (JavaColumn javaColumn : javaColumnList) {
            if (ignoreColumnSet.contains(javaColumn.getName())) {
                continue;
            }
            String columnComment=javaColumn.getComment();
            String columnCommentPre;
            StringBuilder fieldValidateInfo=new StringBuilder();
            if(columnComment.contains("(")){
                columnCommentPre=columnComment.substring(0,columnComment.indexOf('('));
            }else{
                columnCommentPre=columnComment;
            }
            //spring valid注解
            //不为空验证
            if(!javaColumn.isNull()){
                if(config.isNeedBeanValidate()) {
                    fieldSb.append(blank);
                    if ("String".equals(javaColumn.getType())) {
                        fieldSb.append("@NotBlank(message = \"[");
                    } else {
                        fieldSb.append("@NotNull(message = \"[");
                    }
                    fieldSb.append(columnCommentPre);
                    fieldSb.append("]不能为空");
                    fieldSb.append("\")");
                    fieldSb.append("\n");
                }

                if(fieldValidateInfo.length()>0){
                    fieldValidateInfo.append(",");
                }
                fieldValidateInfo.append("不能为空");
            }

            //长度验证
            if("String".equals(javaColumn.getType())){
                if(config.isNeedBeanValidate()) {
                    fieldSb.append(blank);
                    fieldSb.append("@Size(max = ");
                    fieldSb.append(javaColumn.getStrLen());
                    fieldSb.append(",message = \"[");
                    fieldSb.append(columnCommentPre);
                    fieldSb.append("]长度不能超过");
                    fieldSb.append(javaColumn.getStrLen());
                    fieldSb.append("\")");
                    fieldSb.append("\n");
                }

                if(fieldValidateInfo.length()>0){
                    fieldValidateInfo.append(",");
                }
                fieldValidateInfo.append("长度不能超过");
                fieldValidateInfo.append(javaColumn.getStrLen());
            }


            //字段swagger注解
            fieldSb.append(blank);
            fieldSb.append("@ApiModelProperty(value = \"");
            fieldSb.append(columnComment);
            if(fieldValidateInfo.length()>0){
                fieldValidateInfo.append(")");
                fieldValidateInfo.insert(0,"(");
                fieldSb.append(fieldValidateInfo);
            }
            fieldSb.append("\")");
            fieldSb.append("\n");

            //字段定义
            fieldSb.append(blank);
            fieldSb.append("private ");
            fieldSb.append(javaColumn.getType());
            fieldSb.append(" ");
            fieldSb.append(javaColumn.getName());
            fieldSb.append(";");
            fieldSb.append("\n");
            fieldSb.append("\n");
        }

        javaColumnList.forEach(javaColumn -> {
            if (ignoreColumnSet.contains(javaColumn.getName())) {
                return;
            }
            String setMethodName = "set" + javaColumn.getName().substring(0, 1).toUpperCase() + javaColumn.getName().substring(1);
            String getMethodName = "get" + javaColumn.getName().substring(0, 1).toUpperCase() + javaColumn.getName().substring(1);
            //setMethod
            methodSb.append(blank);
            methodSb.append("public void ");
            methodSb.append(setMethodName);
            methodSb.append("(");
            methodSb.append(javaColumn.getType());
            methodSb.append(" ");
            methodSb.append(javaColumn.getName());
            methodSb.append("){");
            methodSb.append("\n");
            methodSb.append(blank);
            methodSb.append("    this.");
            methodSb.append(javaColumn.getName());
            methodSb.append("=");
            methodSb.append(javaColumn.getName());
            methodSb.append(";");
            methodSb.append("\n");
            methodSb.append(blank);
            methodSb.append("}");
            methodSb.append("\n");
            methodSb.append("\n");
            //getMethod
            methodSb.append(blank);
            methodSb.append("public ");
            methodSb.append(javaColumn.getType());
            methodSb.append(" ");
            methodSb.append(getMethodName);
            methodSb.append("(){");
            methodSb.append("\n");
            methodSb.append(blank);
            methodSb.append("    return this.");
            methodSb.append(javaColumn.getName());
            methodSb.append(";");
            methodSb.append("\n");
            methodSb.append(blank);
            methodSb.append("}");
            methodSb.append("\n");
            methodSb.append("\n");
        });
        config.getValueMap().put("beanFields", fieldSb.toString());
        config.getValueMap().put("beanMethods", methodSb.toString());
    }

    public static void main(String[] args) {
        String path = "/Users/baichangda/bcd/workspace/MySpringBootFrame/Sys/src/main/java/com/bcd/sys";
        List<Config> list = Arrays.asList(
                new Config(path,
                        new TableConfig("Role", "角色", "t_sys_role").setNeedCreateServiceFile(false).setNeedCreateControllerFile(true)
                        .setNeedCreateRepositoryFile(false).setNeedBeanValidate(true).setNeedCreateBeanFile(false).setNeedParamValidate(true)
                )
        );
        CodeGenerator.generate(list);
    }
}
