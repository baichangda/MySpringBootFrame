package com.bcd.base.support_jdbc.conn;

import com.bcd.base.exception.BaseException;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * 标准jdbc语法封装
 * 实体类class必须满足如下要求
 * 1、主键默认为id
 * 2、可以是class或者record
 * 3、当为class时候、必须提供参数为空的构造方法、且只会处理非static和非final字段
 * 4、字段映射数据库字段会将驼峰格式转换为下划线格式、例如createTime->create_time
 */
@SuppressWarnings("unchecked")
public class Conn {
    private record ClassInfo<T>(boolean isRecord, Constructor<T> constructor,
                                LinkedHashMap<String, FieldInfo> columnName_field) {

    }

    private record FieldInfo(Field field, String columnName, int index) {

    }

    private final static HashMap<Class<?>, ClassInfo<?>> class_info = new HashMap<>();

    private static <T> ClassInfo<T> getClassInfo(Class<T> clazz) {
        return (ClassInfo<T>) class_info.computeIfAbsent(clazz, c -> {
            try {
                if (clazz.isRecord()) {
                    List<Class<?>> fieldTypeList = new ArrayList<>();
                    LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<>();
                    do {
                        Field[] fields = c.getDeclaredFields();
                        int index = 0;
                        for (Field field : fields) {
                            int modifiers = field.getModifiers();
                            if (Modifier.isStatic(modifiers)) {
                                continue;
                            }
                            field.setAccessible(true);
                            String columnName = StringUtil.camelCaseToSplitChar(field.getName(), '_');
                            map.put(columnName, new FieldInfo(field, columnName, index++));
                            fieldTypeList.add(field.getType());
                        }
                        c = c.getSuperclass();
                    } while (c != null);
                    return new ClassInfo<>(true, clazz.getConstructor(fieldTypeList.toArray(new Class[0])), map);
                } else {
                    LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<>();
                    do {
                        Field[] fields = c.getDeclaredFields();
                        int index = 0;
                        for (Field field : fields) {
                            int modifiers = field.getModifiers();
                            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                                continue;
                            }
                            field.setAccessible(true);
                            String columnName = StringUtil.camelCaseToSplitChar(field.getName(), '_');
                            map.put(columnName, new FieldInfo(field, columnName, index++));
                        }
                        c = c.getSuperclass();
                    } while (c != null);
                    return new ClassInfo<>(false, clazz.getConstructor(), map);
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                throw BaseException.get(ex);
            }
        });
    }

    private final Connection connection;

    public Conn(String url) {
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw BaseException.get(e);
        }
    }


    public synchronized <T> List<T> list(String sql, Class<T> clazz, Object... args) {
        try {
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
                List<T> list = new ArrayList<>();
                ClassInfo<T> classInfo = getClassInfo(clazz);
                LinkedHashMap<String, FieldInfo> columnName_field = classInfo.columnName_field;
                if (classInfo.isRecord) {
                    while (rs.next()) {
                        Object[] arr = new Object[columnName_field.size()];
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            String columnName = metaData.getColumnName(i);
                            FieldInfo fieldInfo = columnName_field.get(columnName);
                            if (fieldInfo != null) {
                                arr[fieldInfo.index] = rs.getObject(i);
                            }
                        }
                        T t = classInfo.constructor.newInstance(arr);
                        list.add(t);
                    }
                } else {
                    while (rs.next()) {
                        T t = classInfo.constructor.newInstance();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            String columnName = metaData.getColumnName(i);
                            FieldInfo fieldInfo = columnName_field.get(columnName);
                            if (fieldInfo != null) {
                                fieldInfo.field.set(t, rs.getObject(i));
                            }
                        }
                        list.add(t);
                    }
                }
                return list;
            }
        } catch (Exception e) {
            throw BaseException.get(e);
        }
    }

    public synchronized boolean execute(String sql, Object... args) {
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.execute();
        } catch (SQLException e) {
            throw BaseException.get(e);
        }
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
    public synchronized <T> int insert(InsertSqlResult<T> insertSqlResult, T... ts) {
        try {
            return insertSqlResult.insertBatch(connection, ts);
        } catch (SQLException | IllegalAccessException e) {
            throw BaseException.get(e);
        }
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
    public synchronized <T> int update(UpdateSqlResult<T> updateSqlResult, T... ts) {
        try {
            return updateSqlResult.updateBatch(connection, ts);
        } catch (SQLException | IllegalAccessException e) {
            throw BaseException.get(e);
        }
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
        final Collection<FieldInfo> allFields = getClassInfo(clazz).columnName_field.values();
        final List<FieldInfo> insertFieldList = new ArrayList<>();
        for (FieldInfo fieldInfo : allFields) {
            if ((fieldFilter == null || fieldFilter.apply(fieldInfo.field))) {
                insertFieldList.add(fieldInfo);
            }
        }
        final StringJoiner sj1 = new StringJoiner(",");
        final StringJoiner sj2 = new StringJoiner(",");
        for (FieldInfo fieldInfo : insertFieldList) {
            sj1.add(fieldInfo.columnName);
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
        return new InsertSqlResult<>(clazz, "insert into " + table + "(" + sj1 + ")", "(" + sj2 + ")", insertFieldList.stream().map(e -> e.field).toArray(Field[]::new));
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
        final Collection<FieldInfo> allFields = getClassInfo(clazz).columnName_field.values();
        final List<FieldInfo> updateFieldList = new ArrayList<>();
        final Map<String, FieldInfo> whereMap = new HashMap<>();
        final Set<String> whereFieldSet = Set.of(whereFieldNames);
        for (FieldInfo fieldInfo : allFields) {
            if (fieldFilter == null || fieldFilter.apply(fieldInfo.field)) {
                updateFieldList.add(fieldInfo);
            }
            if (whereFieldSet.contains(fieldInfo.field.getName())) {
                whereMap.put(fieldInfo.field.getName(), fieldInfo);
            }
        }

        final List<FieldInfo> whereFieldList = new ArrayList<>();
        final Set<String> whereNullFieldSet = new HashSet<>();
        for (String whereFieldName : whereFieldNames) {
            if (whereMap.containsKey(whereFieldName)) {
                whereFieldList.add(whereMap.get(whereFieldName));
            } else {
                whereNullFieldSet.add(whereFieldName);
            }
        }
        if (!whereNullFieldSet.isEmpty()) {
            throw BaseException.get("whereField[{}] not exist", Arrays.toString(whereNullFieldSet.toArray(new String[0])));
        }

        final StringBuilder sb = new StringBuilder("update ");
        sb.append(table);
        sb.append(" set ");
        for (int i = 0; i < updateFieldList.size(); i++) {
            final FieldInfo fieldInfo = updateFieldList.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fieldInfo.columnName);
            sb.append("=?");
        }
        sb.append(" where ");
        for (int i = 0; i < whereFieldList.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            final FieldInfo fieldInfo = whereFieldList.get(i);
            sb.append(fieldInfo.columnName);
            sb.append("=?");
        }
        return new UpdateSqlResult<>(sb.toString(), updateFieldList.stream().map(e -> e.field).toArray(Field[]::new), whereFieldList.stream().map(e -> e.field).toArray(Field[]::new));
    }

//    public record Test(long id, String userName, String remark) {
//
//    }

    public static class Test {
        public long id;
        public String userName;
        public String remark;

        public Test(long id, String userName, String remark) {
            this.id = id;
            this.userName = userName;
            this.remark = remark;
        }

        public Test() {
        }
    }

    public static void main(String[] args) {
        Conn conn = new Conn("jdbc:sqlite::memory:");
//        Conn conn = new Conn("jdbc:sqlite:test.db");
        InsertSqlResult<Test> insertSqlResult = Conn.toInsertSqlResult(Test.class, "t_test", f -> !f.getName().equals("id"));
        UpdateSqlResult<Test> updateSqlResult = Conn.toUpdateSqlResult(Test.class, "t_test", f -> !f.getName().equals("id"), "id");
        conn.execute("""
                create table t_test(
                id integer primary key autoincrement,
                user_name varchar(100),
                remark varchar(200)
                )
                """);
        conn.insert(insertSqlResult, new Test(1, "张三", "我是张三"));
        conn.update(updateSqlResult, new Test(1, "张三2", "我是李四"));
        List<Test> list = conn.list("select * from t_test", Test.class);
        System.out.println(JsonUtil.toJson(list));
    }
}
