package com.bcd.base.support_jpa.dbinfo.pgsql.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.SpringUtil;
import com.bcd.base.util.StringUtil;
import com.bcd.base.support_jpa.dbinfo.data.DBInfo;
import com.bcd.base.support_jpa.dbinfo.pgsql.bean.ColumnsBean;
import com.bcd.base.support_jpa.dbinfo.pgsql.bean.TablesBean;
import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DBInfoUtil {
    /**
     * 获取对应spring数据库连接
     *
     * @return
     */
    public static Connection getSpringConn() {
        DBInfo dbInfo = getDBProps();
        try {
            return DriverManager.getConnection(dbInfo.getUrl(), dbInfo.getUsername(), dbInfo.getPassword());
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取spring配置文件中的数据库信息,并将获取到的url转换成information_schema
     * <p>
     * 获取配置文件中
     * spring.datasource.url
     * spring.datasource.username
     * spring.datasource.password
     * 所以以上必须有数据
     *
     * @return
     */
    public static DBInfo getDBProps() {
        try {
            final JsonNode[] props = SpringUtil.getSpringProps("spring.datasource.url"
                    , "spring.datasource.username"
                    , "spring.datasource.password");
            String url = props[0].asText();
            String username = props[0].asText();
            String password = props[0].asText();
            int index = url.indexOf('?');
            String pre;
            if (index == -1) {
                pre = url;
            } else {
                pre = url.substring(0, index);
            }
            String propDbName = pre.substring(pre.lastIndexOf('/') + 1);
            return new DBInfo(url, username, password, propDbName);

        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 获取对应ip:port的information_schema的数据库连接
     * jdbc:postgresql://db.hbluewhale.com:12921/test_bcd
     *
     * @param url      127.0.0.1:3306
     * @param username root
     * @param password root
     * @param db       test
     * @return
     */
    public static Connection getConn(String url, String username, String password, String db) {
        try {
            return DriverManager.getConnection("jdbc:postgresql://" + url + "/" + db, username, password);
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
        String sql = "SELECT relname AS table_name,CAST(obj_description(relfilenode,'pg_class') AS varchar) AS table_comment FROM pg_class c\n" +
                "WHERE relname IN (SELECT tablename FROM pg_tables WHERE schemaname='public' AND POSITION('_2' IN tablename)=0);";
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
    public static TablesBean findTable(Connection conn, String dbName, String tableName) {
        TablesBean res;
        String sql = "SELECT relname AS table_name,CAST(obj_description(relfilenode,'pg_class') AS varchar) AS table_comment FROM pg_class c\n" +
                "WHERE relname IN (SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename=? AND POSITION('_2' IN tablename)=0);";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<TablesBean> dataList = parseResult(rs, TablesBean.class);
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
                "INNER JOIN pg_description d ON b.attrelid=d.objoid AND d.objsubid=b.attnum\n" +
                "WHERE\n" +
                "    a.relname = ? \n" +
                "\t\tAND c.table_catalog=? \n" +
                "\t\tAND c.table_name=?;";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            pstsm.setString(2, dbName);
            pstsm.setString(3, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                res = parseResult(rs, ColumnsBean.class);
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
                "INNER JOIN pg_description e ON c.attrelid=e.objoid AND e.objsubid=c.attnum\n" +
                "WHERE\n" +
                "    b.relname = ?\n" +
                "\t\tAND a.contype = 'p' \n" +
                "\t\tAND d.table_catalog=?\n" +
                "\t\tAND d.table_name=?";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, tableName);
            pstsm.setString(2, dbName);
            pstsm.setString(3, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<ColumnsBean> res = parseResult(rs, ColumnsBean.class);
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

    public static <T> List<T> parseResult(ResultSet rs, Class<T> resultType) {
        try {
            ResultSetMetaData headData = rs.getMetaData();
            int columnCount = headData.getColumnCount();
            List<T> res = new ArrayList<>();
            while (rs.next()) {
                T t = resultType.newInstance();
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
                    Field field = resultType.getDeclaredField(columnName.toLowerCase());
                    field.setAccessible(true);
                    field.set(t, val);
                }
                res.add(t);
            }
            return res;
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
