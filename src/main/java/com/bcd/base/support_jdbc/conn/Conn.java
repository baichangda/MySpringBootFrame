package com.bcd.base.support_jdbc.conn;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class Conn {
    private final static HashMap<Class<?>, Map<String, Field>> class_fields = new HashMap<>();

    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        if (clazz.isRecord()) {
            throw BaseRuntimeException.getException("Record[{}] not support", clazz.getName());
        }
        return class_fields.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new HashMap<>();
            do {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                        continue;
                    }
                    field.setAccessible(true);
                    map.put(field.getName(), field);
                }
                c = c.getSuperclass();
            } while (c != null);
            return map;
        });
    }

    private final Connection connection;

    public Conn(String url) {
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public synchronized <T> List<T> list(String sql, Class<T> clazz, Object... args) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();

        if (clazz == Map.class) {
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    map.put(columnName, rs.getObject(i));
                }
                list.add(map);
            }
            return (List<T>) list;
        } else {
            Map<String, Field> fieldMap = getFieldMap(clazz);
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                T t = clazz.getConstructor().newInstance();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Field field = fieldMap.get(columnName);
                    if (field != null) {
                        field.set(t, rs.getObject(i));
                    }
                }
                list.add(t);
            }
            return list;
        }
    }

    public synchronized boolean execute(String sql, Object... args) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        return ps.execute();
    }

    /**
     * id 作为主键、自增
     *
     * @param insertSqlResult
     * @param ts
     * @param <T>
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public synchronized <T> int insert(InsertSqlResult<T> insertSqlResult, T... ts) throws SQLException, IllegalAccessException {
        return insertSqlResult.insertBatch(connection, ts);
    }

    /**
     * id作为主键、更新条件
     *
     * @param updateSqlResult
     * @param ts
     * @param <T>
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public synchronized <T> int update(UpdateSqlResult<T> updateSqlResult, T... ts) throws SQLException, IllegalAccessException {
        return updateSqlResult.updateBatch(connection, ts);
    }

    public record InsertSqlResult<T>(Class<T> clazz, String sqlPrefix, String sqlSuffix, Field[] insertFields) {
        public int insertBatch(Connection connection, T... ts) throws SQLException, IllegalAccessException {
            if (ts.length == 0) {
                return 0;
            }
            List<Object> args = new ArrayList<>(insertFields.length * ts.length);
            StringBuilder sql = new StringBuilder(sqlPrefix);
            sql.append(" VALUES ");
            for (int i = 0; i < ts.length; i++) {
                if (i != 0) {
                    sql.append(",");
                }
                sql.append(sqlSuffix);
                for (Field field : insertFields) {
                    args.add(field.get(ts[i]));
                }
            }
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            return ps.executeUpdate();
        }
    }

    /**
     * 将class转换为insert sql语句对象
     * 会获取父类的字段
     * insert字段会去除掉静态字段
     *
     * @param clazz       实体类class
     * @param table       表名
     * @param fieldFilter 字段名过滤器、false则排除掉、会应用于insert字段
     * @param <T>
     * @return
     */
    public static <T> InsertSqlResult<T> toInsertSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter) {
        final Collection<Field> allFields = getFieldMap(clazz).values();
        final List<Field> insertFieldList = new ArrayList<>();
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && (fieldFilter == null || fieldFilter.apply(field))) {
                insertFieldList.add(field);
            }
        }
        final StringJoiner sj1 = new StringJoiner(",");
        final StringJoiner sj2 = new StringJoiner(",");
        for (Field field : insertFieldList) {
            field.setAccessible(true);
            sj1.add(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            sj2.add("?");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(table);
        sb.append("(");
        sb.append(sj1);
        sb.append(") values(");
        sb.append(sj2);
        sb.append(")");
        return new InsertSqlResult<>(clazz, "insert into " + table + "(" + sj1 + ")", "(" + sj2 + ")", insertFieldList.toArray(new Field[0]));
    }


    public record UpdateSqlResult<T>(String sql, Field[] updateFields, Field[] whereFields) {
        public int updateBatch(Connection connection, T... ts) throws SQLException, IllegalAccessException {
            if (ts.length == 0) {
                return 0;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> args = new ArrayList<>((updateFields.length + whereFields.length) * ts.length);
            for (T t : ts) {
                sb.append(sql);
                sb.append(";");
                for (Field field : updateFields) {
                    args.add(field.get(t));
                }
                for (Field field : whereFields) {
                    args.add(field.get(t));
                }
            }
            PreparedStatement ps = connection.prepareStatement(sb.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            return ps.executeUpdate();
        }
    }


    /**
     * 将class转换为update sql语句对象
     * 会获取父类的所有字段
     * update字段会去除掉静态字段
     *
     * @param clazz           实体类
     * @param table           表名
     * @param fieldFilter     字段名过滤器、false则排除掉、会应用于update字段
     * @param whereFieldNames where字段名
     * @param <T>
     * @return
     */
    public static <T> UpdateSqlResult<T> toUpdateSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter, String... whereFieldNames) {
        final Collection<Field> allFields = getFieldMap(clazz).values();
        final List<Field> updateFieldList = new ArrayList<>();
        final Map<String, Field> whereMap = new HashMap<>();
        final Set<String> whereColumnSet = Set.of(whereFieldNames);
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && (fieldFilter == null || fieldFilter.apply(field))) {
                updateFieldList.add(field);
            }
            if (whereColumnSet.contains(field.getName())) {
                whereMap.put(field.getName(), field);
            }
        }

        final List<Field> whereFieldList = new ArrayList<>();
        final Set<String> whereNullFieldSet = new HashSet<>();
        for (String whereColumn : whereFieldNames) {
            if (whereMap.containsKey(whereColumn)) {
                whereFieldList.add(whereMap.get(whereColumn));
            } else {
                whereNullFieldSet.add(whereColumn);
            }
        }
        if (!whereNullFieldSet.isEmpty()) {
            throw BaseRuntimeException.getException("whereField[{}] not exist", Arrays.toString(whereNullFieldSet.toArray(new String[0])));
        }

        final StringBuilder sb = new StringBuilder("update ");
        sb.append(table);
        sb.append(" set ");
        for (int i = 0; i < updateFieldList.size(); i++) {
            final Field field = updateFieldList.get(i);
            field.setAccessible(true);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            sb.append("=?");
        }
        sb.append(" where ");
        for (int i = 0; i < whereFieldList.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            final Field field = whereFieldList.get(i);
            field.setAccessible(true);
            sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            sb.append("=?");
        }
        return new UpdateSqlResult<>(sb.toString(), updateFieldList.toArray(new Field[0]), whereFieldList.toArray(new Field[0]));
    }

    public static class Test {
        public long id;
        public String name;
        public String remark;

        public Test() {
        }

        public Test(long id, String name, String remark) {
            this.id = id;
            this.name = name;
            this.remark = remark;
        }
    }

    public static void main(String[] args) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Conn conn = new Conn("jdbc:sqlite::memory:");
//        Conn conn = new Conn("jdbc:sqlite:test.db");
        InsertSqlResult<Test> insertSqlResult = Conn.toInsertSqlResult(Test.class, "t_test", f -> !f.getName().equals("id"));
        UpdateSqlResult<Test> updateSqlResult = Conn.toUpdateSqlResult(Test.class, "t_test", f -> !f.getName().equals("id"), "id");
        conn.execute("""
                create table t_test(
                id integer primary key autoincrement,
                name varchar(100),
                remark varchar(200)
                )
                """);
        conn.insert(insertSqlResult, new Test(1, "张三", "我是张三"));
        conn.update(updateSqlResult, new Test(1, "张三", "我是李四"));
        List<Test> list = conn.list("select * from t_test", Test.class);
        System.out.println(JsonUtil.toJson(list));
    }
}
