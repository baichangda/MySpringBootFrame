package com.bcd.rdb.dbinfo.mysql.util;

import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * 此帮助类的所有api均会读取application-*.yml的数据库配置,并将数据库切换为information_schema
 */
public class DBInfoUtil {

    private final static String SPRING_PROPERTIES_PATH = System.getProperty("user.dir") + "/src/main/resources/application.yml";

    private final static String DB_INFO_SCHEMA = "information_schema";

    /**
     * 获取spring配置文件中的数据库信息,并将获取到的url转换成information_schema
     *
     * @return
     */
    public static Map<String, Object> getDBProps() {
        try {
            Map<String, Object> props = new HashMap<>();
            Yaml yaml = new Yaml();
            //1、加载spring配置
            LinkedHashMap dataMap = yaml.load(new FileInputStream(Paths.get(SPRING_PROPERTIES_PATH).toFile()));
            LinkedHashMap springMap = (LinkedHashMap) dataMap.get("spring");
            LinkedHashMap dataSourceMap = (LinkedHashMap) springMap.get("datasource");
            //1.1、取出配置文件后缀
            String suffix = (String) springMap.get("profiles.active");
            if (!StringUtils.isEmpty(suffix)) {
                //1.2、如果有激活的配置文件,则加载
                String activePathStr = SPRING_PROPERTIES_PATH.substring(0, SPRING_PROPERTIES_PATH.lastIndexOf('.')) + "-" + suffix + "." + SPRING_PROPERTIES_PATH.substring(SPRING_PROPERTIES_PATH.indexOf('.') + 1);
                Path activePath = Paths.get(activePathStr);
                if (activePath.toFile().exists()) {
                    dataMap = yaml.load(new FileInputStream(activePath.toFile()));
                    springMap = (LinkedHashMap) dataMap.get("spring");
                    dataSourceMap = (LinkedHashMap) springMap.get("datasource");
                }
            }
            //2、取出值
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();
            String pre = url.substring(0, url.indexOf('?'));
            String propDbName = pre.substring(pre.lastIndexOf('/') + 1);
            String dbInfoUrl = url.replace("/" + propDbName, "/" + DB_INFO_SCHEMA);
            props.put("url", dbInfoUrl);
            props.put("dbName", propDbName);
            props.put("username", username);
            props.put("password", password);
            return props;
        } catch (FileNotFoundException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取对应spring数据库的information_schema的数据库连接
     *
     * @return
     */
    public static Connection getConn() {
        Map<String, Object> dbProps = getDBProps();
        String url = dbProps.get("url").toString();
        String username = dbProps.get("username").toString();
        String password = dbProps.get("password").toString();
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static List<Map<String, Object>> parseResult(ResultSet rs) {
        try {
            List<Map<String, Object>> res = new ArrayList<>();
            ResultSetMetaData headData = rs.getMetaData();
            int columnCount = headData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> jsonObject = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object val = null;
                    int columnType = headData.getColumnType(i);
                    String columnName = headData.getColumnName(i);
                    switch (columnType) {
                        case Types.BIGINT: {
                            val = rs.getLong(i);
                            break;
                        }
                        case Types.INTEGER: {
                            val = rs.getInt(i);
                            break;
                        }
                        case Types.VARCHAR: {
                            val = rs.getString(i);
                            break;
                        }
                        case Types.LONGVARCHAR: {
                            val = rs.getString(i);
                            break;
                        }
                        case Types.DATE: {
                            val = rs.getDate(i);
                            break;
                        }
                        case Types.TIMESTAMP: {
                            val = rs.getDate(i);
                            break;
                        }
                    }
                    jsonObject.put(columnName, val);
                }
                res.add(jsonObject);
            }
            return res;
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取指定数据库名称下面的所有表
     *
     * @param dbName
     * @return
     */
    public static List<Map<String, Object>> findTables(String dbName) {
        List<Map<String, Object>> res;
        String sql = "select * from tables where table_schema=?";
        try (Connection conn = getConn();
             PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            try (ResultSet rs = pstsm.executeQuery()) {
                res = parseResult(rs);
            }
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
        return res;
    }

    /**
     * 获取指定数据库名称下面的指定的表
     *
     * @param dbName
     * @return
     */
    public static Map<String, Object> findTable(String dbName, String tableName) {
        Map<String, Object> res;
        String sql = "select * from tables where table_schema=? and table_name=?";
        try (Connection conn = getConn();
             PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<Map<String, Object>> jsonArray = parseResult(rs);
                if (jsonArray == null || jsonArray.isEmpty()) {
                    return null;
                }
                res = jsonArray.get(0);
                return res;
            }
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 找到指定数据库表下面的所有列
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public static List<Map<String, Object>> findColumns(String dbName, String tableName) {
        List<Map<String, Object>> res;
        String sql = "select * from columns where table_schema=? and table_name=?";
        try (Connection conn = getConn();
             PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                res = parseResult(rs);
            }
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
        return res;
    }

    /**
     * 获取指定数据库表的主键
     */
    public static Map<String, Object> findPKColumn(String dbName, String tableName) {
        String sql = "select a.* from " +
                "(select * from columns where table_schema=? and table_name=?) a inner join" +
                "(select COLUMN_NAME from STATISTICS where table_schema=? and table_name=? and index_name='PRIMARY') b on a.COLUMN_NAME=b.COLUMN_NAME";
        try (Connection conn = getConn();
             PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            pstsm.setString(3, dbName);
            pstsm.setString(4, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<Map<String, Object>> res = parseResult(rs);
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
