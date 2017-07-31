package com.base.code;

import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/7/28.
 */
public class CodeGenerator {
    private static Connection conn;


    private static String url="jdbc:mysql://127.0.0.1:3306/test?characterEncoding=utf8&useSSL=false";
    private static String username="root";
    private static String password="root";

    private final static String TEMPLATE_DIR_PATH = "template";

    public static void generate(List<ConfigProperties> configList) {
        configList.forEach(config -> generate(config));
    }

    private  static void generate(ConfigProperties config) {
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
                    newPath.append(File.pathSeparator);
                    newPath.append(sub.toString().replace("\\", File.pathSeparator));
                    Path newPathObj = Paths.get(newPath.toString());
                    Files.deleteIfExists(newPathObj);
                    createPath(newPathObj, 1);
                    //2、获取模版文件流@{template}
                    try (
                            BufferedReader br = new BufferedReader(new FileReader(file.toFile()));
                            PrintWriter pw = new PrintWriter(new FileWriter(newPathObj.toFile(), true))
                    ) {
                        String[] lineStr = new String[1];
                        while ((lineStr[0] = br.readLine()) != null) {
                            valueMap.forEach((k, v) -> lineStr[0] = lineStr[0].replaceAll("${" + k + "}", v));
                            pw.println(lineStr[0]);
                        }
                        pw.flush();
                    }
                    //3
                    System.err.println(newPathObj.getFileName() + " generate successed!");
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
                Files.createDirectories(pathObj.getParent());
                Files.createFile(pathObj);
            } else {
                Files.createDirectories(pathObj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathObj;
    }

    private static void initConfig(ConfigProperties config) {
        initBeanConfig(config);
        initRepositoryConfig(config);
        initServiceConfig(config);
        initControllerConfig(config);
        initAfter(config);
    }

    private static void initAfter(ConfigProperties config){
        if(config.isNeedCreateInfo()){
            config.getValueMap().put("superBean","BaseBean");
        }else{
            config.getValueMap().put("superBean","AbstractBean");
        }
    }

    private static void initServiceConfig(ConfigProperties config) {

    }

    private static void initRepositoryConfig(ConfigProperties config) {

    }

    private static void initControllerConfig(ConfigProperties config) {

    }

    /**
     * 根据ConfigProperties初始化Bean的配置
     *
     * @param config
     * @return
     */
    private static void initBeanConfig(ConfigProperties config) {
        String tableName = config.getValueMap().get("tableName");
        List<DBColumn> dbColumnList = findColumns(tableName);
        List<JavaColumn> javaColumnList = dbColumnList.stream().map(dbColumn -> dbColumn.toJavaColumn()).collect(Collectors.toList());
        StringBuffer fieldSb = new StringBuffer();
        StringBuffer methodSb = new StringBuffer();
        javaColumnList.forEach(javaColumn -> {
            fieldSb.append("//");
            fieldSb.append(javaColumn.getComment());
            fieldSb.append("\n");

            fieldSb.append("private ");
            fieldSb.append(javaColumn.getType());
            fieldSb.append(" ");
            fieldSb.append(javaColumn.getName());
            fieldSb.append(";");
            fieldSb.append("\n");
        });

        javaColumnList.forEach(javaColumn -> {
            String setMethodName = "set" + javaColumn.getName().substring(0, 1).toUpperCase() + javaColumn.getName().substring(1);
            String getMethodName = "get" + javaColumn.getName().substring(0, 1).toUpperCase() + javaColumn.getName().substring(1);
            //setMethod
            methodSb.append("public void ");
            methodSb.append(setMethodName);
            methodSb.append("(");
            methodSb.append(javaColumn.getType());
            methodSb.append(" ");
            methodSb.append(javaColumn.getName());
            methodSb.append("){");
            methodSb.append("\n");
            methodSb.append("    this.");
            methodSb.append(javaColumn.getName());
            methodSb.append("=");
            methodSb.append(javaColumn.getName());
            methodSb.append(";");
            methodSb.append("\n");
            methodSb.append("}");
            methodSb.append("\n");
            //getMethod
            methodSb.append("public ");
            methodSb.append(javaColumn.getType());
            methodSb.append(" ");
            methodSb.append(getMethodName);
            methodSb.append("(){");
            methodSb.append("\n");
            methodSb.append("    return this.");
            methodSb.append(javaColumn.getName());
            methodSb.append(";");
            methodSb.append("\n");
            methodSb.append("}");
            methodSb.append("\n");
        });
        config.getValueMap().put("${field}", fieldSb.toString());
        config.getValueMap().put("${method}", methodSb.toString());
        config.getDataMap().put("fieldList", javaColumnList);
    }

    private static List<DBColumn> findColumns(String tableName) {
        List<DBColumn> columnList = new ArrayList<>();
        String pre = url.substring(0, url.indexOf("?"));
        String dbName = pre.substring(pre.lastIndexOf("/") + 1);
        String dbInfoUrl = url.replace("/" + dbName, "/information_schema");
        String sql = "select * from columns where table_name=?";
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
        PreparedStatement pstsm=null;
        ResultSet rs=null;
        try {
            pstsm= conn.prepareStatement(sql);
            pstsm.setString(0, tableName);
            rs = pstsm.executeQuery();
            while (rs.next()) {
                DBColumn dbColumn = new DBColumn();
                dbColumn.setName(rs.getString("COLUMN_NAME"));
                dbColumn.setType(rs.getString("DATA_TYPE"));
                dbColumn.setComment(rs.getString("COLUMN_COMMENT"));
                columnList.add(dbColumn);
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally{
            try {
                if(pstsm!=null){
                    pstsm.close();

                }
                if(rs!=null){
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columnList;
    }

    public static void main(String[] args) {
        ConfigProperties configProperties=new ConfigProperties();
        configProperties.setDirPath("d:/test");
        configProperties.setNeedCreateInfo(true);
        CodeGenerator.generate(configProperties);
    }
}
