package com.bcd.base.support_jpa.dbinfo.mysql.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.dbinfo.data.DBInfo;
import com.bcd.base.support_jpa.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.base.support_jpa.dbinfo.mysql.bean.TablesBean;
import com.bcd.base.util.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 此帮助类的所有api均会读取application-*.yml的数据库配置,并将数据库切换为information_schema
 */
public class DBInfoUtil {

    private final static String DB_INFO_SCHEMA = "information_schema";

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
            final JsonNode[] props = SpringUtil.getSpringPropsInYml("spring.datasource.url"
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
            String dbInfoUrl = url.replace("/" + propDbName, "/" + DB_INFO_SCHEMA);
            return new DBInfo(dbInfoUrl, username, password, propDbName);

        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }


    }

    /**
     * 获取对应spring数据库的information_schema的数据库连接
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
     * 获取对应ip:port的information_schema的数据库连接
     * jdbc:mysql://127.0.0.1:3306/information_schema
     *
     * @param url      127.0.0.1:3306
     * @param username root
     * @param password root
     * @return
     */
    public static Connection getConn(String url, String username, String password) {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + url + "/" + DB_INFO_SCHEMA, username, password);
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
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR: {
                            val = rs.getString(i);
                            break;
                        }
                        case Types.DATE:
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

    /**
     * 获取指定数据库名称下面的所有表
     *
     * @param conn
     * @param dbName
     * @return
     */
    public static List<TablesBean> findTables(Connection conn, String dbName) {
        List<TablesBean> res;
        String sql = "SELECT * FROM tables WHERE table_schema=?";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
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
        String sql = "SELECT * FROM tables WHERE table_schema=? AND table_name=?";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            try (ResultSet rs = pstsm.executeQuery()) {
                List<TablesBean> dataList = parseResult(rs, TablesBean.class);
                if (dataList.isEmpty()) {
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
        String sql = "SELECT * FROM columns WHERE table_schema=? AND table_name=?";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
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
        String sql = "SELECT a.* FROM " +
                "(SELECT * FROM columns WHERE table_schema=? AND table_name=?) a INNER JOIN" +
                "(SELECT column_name FROM statistics WHERE table_schema=? AND table_name=? AND index_name='PRIMARY') b ON a.column_name=b.column_name";
        try (PreparedStatement pstsm = conn.prepareStatement(sql)) {
            pstsm.setString(1, dbName);
            pstsm.setString(2, tableName);
            pstsm.setString(3, dbName);
            pstsm.setString(4, tableName);
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
}
