package com.base.code;


import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/28.
 */
public class CodeGenerator {
    private static Connection conn;


    private static String url;
    private static String username;
    private static String password;

    private final static String TEMPLATE_DIR_PATH = System.getProperty("user.dir") + "/Base/src/main/resources/template";
    private final static String SPRING_PROPERTIES_PATH = System.getProperty("user.dir") + "/src/main/resources/application.yml";


    static {
        initProperties();
    }

    /**
     * 初始化spring配置文件
     */
    private static void initProperties(){
        try {
            Yaml yaml = new Yaml();
            //1、加载spring配置
            LinkedHashMap dataMap = (LinkedHashMap) yaml.load(new FileInputStream(Paths.get(SPRING_PROPERTIES_PATH).toFile()));
            LinkedHashMap springMap = (LinkedHashMap) dataMap.get("spring");
            LinkedHashMap dataSourceMap = (LinkedHashMap) springMap.get("datasource");
            //1.1、取出配置文件后缀
            String suffix = springMap.get("profiles.active").toString();
            if (!StringUtils.isEmpty(suffix)) {
                //1.2、如果有激活的配置文件,则加载
                Path activePath = Paths.get(SPRING_PROPERTIES_PATH + "-" + suffix);
                if (Files.exists(activePath)) {
                    dataMap = (LinkedHashMap) yaml.load(new FileInputStream(activePath.toFile()));
                    springMap = (LinkedHashMap) dataMap.get("spring");
                    dataSourceMap = (LinkedHashMap) springMap.get("datasource");
                }
            }
            //2、取出值
            url = dataSourceMap.get("url").toString();
            username = dataSourceMap.get("username").toString();
            password = dataSourceMap.get("password").toString();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void generate(List<ConfigProperties> configList) {
        configList.forEach(config -> generate(config));
    }

    /**
     * 需要如下参数
     *
     * @param config
     */
    private static void generate(ConfigProperties config) {
        initConfig(config);
        String dirPath = config.getDirPath();
        Map<String, String> valueMap = config.getValueMap();
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
                    StringBuffer newPath = new StringBuffer();
                    newPath.append(dirPath.toString());
                    newPath.append(File.separator);
                    newPath.append(sub.toString());
                    String newPathStr = newPath.toString().replace(".txt", ".java").replace("Template", config.getModuleName());
                    Path newPathObj = Paths.get(newPathStr);
                    Files.deleteIfExists(newPathObj);
                    createPath(newPathObj, 1);
                    //2、获取模版文件流@{template}
                    try (
                            BufferedReader br = new BufferedReader(new FileReader(file.toFile()));
                            PrintWriter pw = new PrintWriter(new FileWriter(newPathObj.toFile(), true))
                    ) {
                        String[] lineStr = new String[1];
                        while ((lineStr[0] = br.readLine()) != null) {
                            valueMap.forEach((k, v) -> lineStr[0] = lineStr[0].replaceAll("@\\{" + k + "\\}", v));
                            pw.println(lineStr[0]);
                        }
                        pw.flush();
                    }
                    System.err.println(newPathObj.toString() + " generate successed!");
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
            e.printStackTrace();
        }
    }

    /**
     * 创建文件或者文件夹
     *
     * @param path
     * @param flag
     * @return
     */
    public static Path createPath(Object path, int flag) {
        Path pathObj;
        if (Path.class.isAssignableFrom(path.getClass())) {
            pathObj = (Path) path;
        } else if (String.class.isAssignableFrom(path.getClass())) {
            pathObj = Paths.get((String) path);
        } else {
            return null;
        }
        if (Files.exists(pathObj)) {
            return pathObj;
        }
        try {
            if (flag == 1) {
                createPath(pathObj.getParent(), 2);
                Files.createFile(pathObj);
            } else {
                Path parentPathObj = pathObj.getParent();
                if (!Files.exists(parentPathObj)) {
                    createPath(parentPathObj, 2);
                }
                Files.createDirectories(pathObj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathObj;
    }

    /**
     * 初始化配置
     *
     * @param config
     */
    private static void initConfig(ConfigProperties config) {
        initBefore(config);
        initBeanConfig(config);
        initRepositoryConfig(config);
        initServiceConfig(config);
        initControllerConfig(config);
        initAfter(config);
    }

    /**
     * 前置初始化
     * 1、将表转换成java字段
     * 2、初始化模块名中文
     * 3、初始化模块名大小写
     * 4、初始化Bean的父类
     *
     * @param config
     */
    private static void initBefore(ConfigProperties config) {
        //1、获取java字段并且存储
        String tableName = config.getTableName();
        List<DBColumn> dbColumnList = findColumns(tableName);
        List<JavaColumn> javaColumnList = dbColumnList.stream().map(dbColumn -> dbColumn.toJavaColumn()).collect(Collectors.toList());
        config.getDataMap().put("fieldList", javaColumnList);
        //2、初始化模块名中文
        config.getValueMap().put("moduleNameCN", config.getModuleNameCN());
        //3、初始化模块名大小写
        initModuleName(config);
        //4、初始化bean父类 ; 忽视的属性set
        Set<String> ignoreColumnSet = new HashSet<>();
        ignoreColumnSet.add("id");
        if (config.isNeedCreateInfo()) {
            config.getValueMap().put("superBean", "BaseBean");
            ignoreColumnSet.add("createTime");
            ignoreColumnSet.add("updateTime");
            ignoreColumnSet.add("createUserId");
            ignoreColumnSet.add("updateUserId");
        } else {
            config.getValueMap().put("superBean", "SuperBaseBean");
        }
        config.getDataMap().put("ignoreColumnSet", ignoreColumnSet);
        //5、解析形成包名
        initPackage(config);
    }

    private static void initPackage(ConfigProperties config) {
        String springSrcPath = "src/main/java/";
        String formatDirPath = config.getDirPath().replaceAll("\\\\", "/");
        if (formatDirPath.contains(springSrcPath)) {
            String pgk = formatDirPath.split(springSrcPath)[1].replaceAll("/", ".");
            config.getValueMap().put("package", pgk);
        }
    }

    /**
     * 后置初始化
     *
     * @param config
     */
    private static void initAfter(ConfigProperties config) {

    }

    /**
     * 初始化service
     *
     * @param config
     */
    private static void initServiceConfig(ConfigProperties config) {
    }

    /**
     * 初始化repository
     *
     * @param config
     */
    private static void initRepositoryConfig(ConfigProperties config) {
    }

    /**
     * 初始化controller
     *
     * @param config
     */
    private static void initControllerConfig(ConfigProperties config) {
        //缩进
        final String blank = "            ";
        List<JavaColumn> javaColumnList = (List<JavaColumn>) config.getDataMap().get("fieldList");
        StringBuffer swaggerParamsSb = new StringBuffer();
        StringBuffer paramsSb = new StringBuffer();
        StringBuffer conditionsSb = new StringBuffer();
        for (int i = 0; i <= javaColumnList.size() - 1; i++) {
            JavaColumn column = javaColumnList.get(i);
            String type = column.getType();
            String param = column.getName();
            String paramBegin = column.getName() + "Begin";
            String paramEnd = column.getName() + "End";
            //1、controllerListSwaggerParams
            if (type.equals("Date")) {
                swaggerParamsSb.append(blank);
                swaggerParamsSb.append("@ApiImplicitParam(name = \"" + paramBegin + "\",value = \"" + column.getComment() + "开始\", dataType = \"" + type + "\",paramType = \"query\")");
                swaggerParamsSb.append(",");
                swaggerParamsSb.append("\n");
                swaggerParamsSb.append(blank);
                swaggerParamsSb.append("@ApiImplicitParam(name = \"" + paramEnd + "\",value = \"" + column.getComment() + "截至\", dataType = \"" + type + "\",paramType = \"query\")");
            } else {
                swaggerParamsSb.append(blank);
                swaggerParamsSb.append("@ApiImplicitParam(name = \"" + param + "\",value = \"" + column.getComment() + "\", dataType = \"" + type + "\",paramType = \"query\")");
            }
            swaggerParamsSb.append(",");
            //2、controllerListParams
            if (type.equals("Date")) {
                paramsSb.append(blank);
                paramsSb.append("@RequestParam(value = \"" + paramBegin + "\",required = false) " + type + " " + paramBegin);
                paramsSb.append(",");
                paramsSb.append("\n");
                paramsSb.append(blank);
                paramsSb.append("@RequestParam(value = \"" + paramEnd + "\",required = false) " + type + " " + paramEnd);
            } else {
                paramsSb.append(blank);
                paramsSb.append("@RequestParam(value = \"" + param + "\",required = false) " + type + " " + param);
            }
            paramsSb.append(",");
            //3、controllerListConditions
            String condition = CodeConst.TYPE_CONDITION_MAPPING.get(column.getType());
            if (type.equals("Date")) {
                conditionsSb.append(blank);
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + paramBegin + ", " + condition + ".Handler.GE)");
                conditionsSb.append(",");
                conditionsSb.append("\n");
                conditionsSb.append(blank);
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + paramEnd + ", " + condition + ".Handler.LE)");
            } else {
                conditionsSb.append(blank);
                conditionsSb.append("new " + condition + "(\"" + param + "\"," + param + ", " + condition + ".Handler.EQUAL)");
            }
            if (i != javaColumnList.size() - 1) {
                conditionsSb.append(",");
                swaggerParamsSb.append("\n");
                paramsSb.append("\n");
                conditionsSb.append("\n");
            }


            config.getValueMap().put("controllerListSwaggerParams", swaggerParamsSb.toString());
            config.getValueMap().put("controllerListParams", paramsSb.toString());
            config.getValueMap().put("controllerListConditions", conditionsSb.toString());
        }
    }

    /**
     * 初始化模块名大小写
     *
     * @param config
     */
    private static void initModuleName(ConfigProperties config) {
        String moduleName = config.getModuleName();
        Character firstChar = moduleName.charAt(0);
        config.getValueMap().put("upperModuleName", Character.toUpperCase(firstChar) + moduleName.substring(1));
        config.getValueMap().put("lowerModuleName", Character.toLowerCase(firstChar) + moduleName.substring(1));
    }

    /**
     * 初始化Bean的配置
     *
     * @param config
     * @return
     */
    private static void initBeanConfig(ConfigProperties config) {
        Set<String> ignoreColumnSet = (Set<String>) config.getDataMap().get("ignoreColumnSet");
        String blank = "    ";
        List<JavaColumn> javaColumnList = (List<JavaColumn>) config.getDataMap().get("fieldList");
        StringBuffer fieldSb = new StringBuffer();
        StringBuffer methodSb = new StringBuffer();
        javaColumnList.forEach(javaColumn -> {
            if (ignoreColumnSet.contains(javaColumn.getName())) {
                return;
            }
            fieldSb.append(blank);
            fieldSb.append("//");
            fieldSb.append(javaColumn.getComment());
            fieldSb.append("\n");

            fieldSb.append(blank);
            fieldSb.append("private ");
            fieldSb.append(javaColumn.getType());
            fieldSb.append(" ");
            fieldSb.append(javaColumn.getName());
            fieldSb.append(";");
            fieldSb.append("\n");
        });

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
        });
        config.getValueMap().put("beanFields", fieldSb.toString());
        config.getValueMap().put("beanMethods", methodSb.toString());

    }

    /**
     * 根据表名和db连接找到对应db字段
     *
     * @param tableName
     * @return
     */
    private static List<DBColumn> findColumns(String tableName) {
        List<DBColumn> columnList = new ArrayList<>();
        String pre = url.substring(0, url.indexOf("?"));
        String dbName = pre.substring(pre.lastIndexOf("/") + 1);
        String dbInfoUrl = url.replace("/" + dbName, "/information_schema");
        String sql = "select * from columns where table_schema=? and table_name=?";
        if (conn == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(dbInfoUrl, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        PreparedStatement pstsm = null;
        ResultSet rs = null;
        try {
            pstsm = conn.prepareStatement(sql);
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            rs = pstsm.executeQuery();
            while (rs.next()) {
                DBColumn dbColumn = new DBColumn();
                dbColumn.setName(rs.getString("COLUMN_NAME"));
                dbColumn.setType(rs.getString("DATA_TYPE"));
                dbColumn.setComment(rs.getString("COLUMN_COMMENT"));
                columnList.add(dbColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstsm != null) {
                    pstsm.close();

                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columnList;
    }

    public static void main(String[] args) {
        String path = "D:\\workSpace\\MySpringBootFrame\\Base\\src\\main\\java\\com\\base\\code";
        List<ConfigProperties> list = Arrays.asList(
                new ConfigProperties(path, "User", "t_sys_user", "用户")
                ,
                new ConfigProperties(path, "Org", "t_sys_org", "组织机构")
                ,
                new ConfigProperties(path, "Role", "t_sys_role", "角色", false)
                ,
                new ConfigProperties(path, "Menu", "t_sys_menu", "菜单")
                ,
                new ConfigProperties(path, "UserRoleRelation", "t_sys_user_role", "用户角色关联")
        );
        CodeGenerator.generate(list);
//        createPath(Paths.get("d:/test/path/a.txt"),1);
    }
}
