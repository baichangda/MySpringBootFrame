package com.bcd.rdb.dbinfo.pgsql.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.rdb.dbinfo.pgsql.bean.TablesBean;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class DBInfoUtil {
    private final static String SPRING_PROPERTIES_PATH = System.getProperty("user.dir") + "/src/main/resources/application.yml";

    /**
     * 获取对应spring数据库连接
     *
     * @return
     */
    public static Connection getSpringConn() {
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


    /**
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
            int index=url.indexOf('?');
            String pre;
            if(index==-1){
                pre = url;
            }else{
                pre = url.substring(0, index);
            }
            String propDbName = pre.substring(pre.lastIndexOf('/') + 1);
            props.put("url", url);
            props.put("dbName", propDbName);
            props.put("username", username);
            props.put("password", password);
            return props;
        } catch (FileNotFoundException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取对应ip:port的information_schema的数据库连接
     * jdbc:postgresql://db.hbluewhale.com:12921/test_bcd
     * @param url 127.0.0.1:3306
     * @param db test
     * @param username root
     * @param password root
     * @return
     */
    public static Connection getConn(String url,String db,String username,String password) {
        try {
            return DriverManager.getConnection("jdbc:postgresql://"+url+"/"+db, username, password);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取指定数据库名称下面的所有表
     *
     * @param conn
     * @param dbName
     * @return
     */
    public static List<TablesBean> findTables(Connection conn, String dbName) {
        List<TablesBean> res;
        String sql = "select relname as table_name,cast(obj_description(relfilenode,'pg_class') as varchar) as table_comment from pg_class c\n" +
                "where relname in (select tablename from pg_tables where schemaname='public' and position('_2' in tablename)=0);";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstsm.executeQuery()) {
                res = parseResult(rs, TablesBean.class);
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
    public static TablesBean findTable(Connection conn,String dbName, String tableName) {
        TablesBean res;
        String sql = "select relname as table_name,cast(obj_description(relfilenode,'pg_class') as varchar) as table_comment from pg_class c\n" +
                "where relname in (select tablename from pg_tables where schemaname='public' and tablename=? and position('_2' in tablename)=0);";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<TablesBean> dataList = parseResult(rs,TablesBean.class);
                if (dataList == null || dataList.isEmpty()) {
                    return null;
                }
                res = dataList.get(0);
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
    public static List<ColumnsBean> findColumns(Connection conn, String dbName, String tableName) {
        List<ColumnsBean> res;
        String sql = "SELECT\n" +
                "    c.*,d.*\n" +
                "FROM\n" +
                "pg_class a\n" +
                "INNER JOIN pg_attribute b ON b.attrelid = a.oid\n" +
                "INNER JOIN information_schema.columns c ON b.attname = c.column_name\n" +
                "INNER join pg_description d on b.attrelid=d.objoid and d.objsubid=b.attnum\n" +
                "WHERE\n" +
                "    a.relname = ? \n" +
                "\t\tand c.table_catalog=? \n" +
                "\t\tand c.table_name=?;";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            pstsm.setString(2, dbName);
            pstsm.setString(3, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                res = parseResult(rs,ColumnsBean.class);
            }
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
        return res;
    }

    /**
     * 获取指定数据库表的主键
     */
    public static ColumnsBean findPKColumn(Connection conn, String dbName, String tableName) {
        String sql = "SELECT\n" +
                "    d.*,e.*\n" +
                "FROM\n" +
                "    pg_constraint a\n" +
                "INNER JOIN pg_class b ON a.conrelid = b.oid\n" +
                "INNER JOIN pg_attribute c ON c.attrelid = b.oid\n" +
                "AND c.attnum = a.conkey [ 1 ]\n" +
                "INNER JOIN information_schema.columns d ON c.attname = d.column_name\n" +
                "INNER join pg_description e on c.attrelid=e.objoid and e.objsubid=c.attnum\n" +
                "WHERE\n" +
                "    b.relname = ?\n" +
                "\t\tAND a.contype = 'p' \n" +
                "\t\tand d.table_catalog=?\n" +
                "\t\tand d.table_name=?";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            pstsm.setString(2, dbName);
            pstsm.setString(3, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<ColumnsBean> res = parseResult(rs,ColumnsBean.class);
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

    public static <T>List<T> parseResult(ResultSet rs,Class<T> resultType) {
        try {
            ResultSetMetaData headData = rs.getMetaData();
            int columnCount = headData.getColumnCount();
            List<T> res = new ArrayList<>();
            while (rs.next()) {
                T t= resultType.newInstance();
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
                    Field field=resultType.getDeclaredField(columnName.toLowerCase());
                    field.setAccessible(true);
                    field.set(t,val);
                }
                res.add(t);
            }
            return res;
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}