package com.bcd.base.support_jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SqlUtil {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtil.class);

    private final static ConcurrentHashMap<String, CCJSqlParser> cache = new ConcurrentHashMap<>();

    public static void main(String[] args) throws JSQLParserException {
        String sql1 = "SELECT \n" +
                "    *\n" +
                "FROM\n" +
                "    t_sys_user a\n" +
                "        INNER JOIN\n" +
                "    t_sys_user_role b ON a.id = b.user_id\n" +
                "WHERE\n" +
                "    username LIKE ? AND (sex = ? OR status=?) AND type in (?,?,?) AND phone in (?,?)";
        List<Object> paramList1 = new ArrayList<>();
        //username
        paramList1.add("%abc%");
        //sex
        paramList1.add(1);
        //status
        paramList1.add(null);
        //type
        paramList1.add(1);
        paramList1.add(null);
        paramList1.add(3);
        //phone
        paramList1.add(null);
        paramList1.add(null);
        Object[] params = paramList1.toArray();
        SqlListResult sqlListResult = replace_nullParam(sql1, params);
        System.out.println(sqlListResult.sql);
        sqlListResult.paramList.forEach(e -> System.out.print(e + "    "));


        System.out.println();
        String sql2 = "SELECT \n" +
                "    *\n" +
                "FROM\n" +
                "    t_sys_user a\n" +
                "        INNER JOIN\n" +
                "    t_sys_user_role b ON a.id = b.user_id\n" +
                "WHERE\n" +
                "    username LIKE :username AND (sex = :sex or status=:status) And type in (:type) And phone in (:phone)";
        Map<String, Object> paramMap2 = new LinkedHashMap<>();
        paramMap2.put("username", "%z%");
        paramMap2.put("sex", 1);
        paramMap2.put("status", null);
        paramMap2.put("type", Arrays.asList(1, null, 3));
        paramMap2.put("phone", new Object[]{null, null});
        SqlMapResult sqlMapResult = replace_nullParam_count_limit(sql2, paramMap2, true, 1, 100);
        System.out.println(sqlMapResult.sql);
        sqlMapResult.paramMap.forEach((k, v) -> {
            if (v.getClass().isArray()) {
                Object[] arr = (Object[]) v;
                System.out.print(k + ":" + Arrays.asList(arr) + "    ");
            } else {
                System.out.print(k + ":" + v + "    ");
            }
        });

        long t1 = System.currentTimeMillis();
        for (int i = 1; i <= 10000; i++) {
            replace_nullParam_count_limit(sql1, true, 1, 20, params);
        }
        long t2 = System.currentTimeMillis();
        for (int i = 1; i <= 10000; i++) {
            replace_nullParam_count_limit(sql2, true, 1, 20, paramMap2);
        }
        long t3 = System.currentTimeMillis();
        for (int i = 1; i <= 10000; i++) {
            getStatement(sql1);
        }
        long t4 = System.currentTimeMillis();
        System.out.printf("\n%dms,%dms,%dms", t2 - t1, t3 - t2, t4 - t3);

//        InsertSqlResult<UserBean> insertSqlResult = toInsertSqlResult(UserBean.class, "t_sys_user", field -> !field.getName().equals("id"), true);
//        System.out.println(insertSqlResult.sql);
//        UpdateSqlResult<UserBean> updateSqlResult = toUpdateSqlResult(UserBean.class, "t_sys_user", field ->
//                !field.getName().equals("id"), true, "id");
//        System.out.println(updateSqlResult.sql);
    }

    private static Statement getStatement(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql,e->{
                e.withAllowComplexParsing(true)
                        .withSquareBracketQuotation(false)
                        .withBackslashEscapeCharacter(false);
            });
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     *
     * @param sql
     * @param params
     * @return
     */
    public static SqlListResult replace_nullParam(String sql, Object... params) {
        return replace_nullParam_count_limit(sql, false, null, null, params);
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、替换sql中的select字段替换为count(*)
     *
     * @param sql
     * @param params
     * @return
     */
    public static SqlListResult replace_nullParam_count(String sql, Object... params) {
        return replace_nullParam_count_limit(sql, true, null, null, params);
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、根据分页参数在sql中加入limit
     *
     * @param sql
     * @param pageNum
     * @param pageSize
     * @param params
     * @return
     */
    public static SqlListResult replace_nullParam_limit(String sql, int pageNum, int pageSize, Object... params) {
        return replace_nullParam_count_limit(sql, false, pageNum, pageSize, params);
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     *
     * @param sql
     * @param paramMap
     * @return
     */
    public static SqlMapResult replace_nullParam(String sql, Map<String, Object> paramMap) {
        return replace_nullParam_count_limit(sql, paramMap, true, null, null);
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、替换sql中的select字段替换为count(*)
     *
     * @param sql
     * @param paramMap
     * @return
     */
    public static SqlMapResult replace_nullParam_count(String sql, Map<String, Object> paramMap) {
        return replace_nullParam_count_limit(sql, paramMap, true, null, null);
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、根据分页参数在sql中加入limit
     *
     * @param sql
     * @param paramMap
     * @param pageNum
     * @param pageSize
     * @return
     */
    public static SqlMapResult replace_nullParam_limit(String sql, Map<String, Object> paramMap, int pageNum, int pageSize) {
        return replace_nullParam_count_limit(sql, paramMap, false, pageNum, pageSize);
    }

    /**
     * 1、根据分页参数在sql中加入limit
     *
     * @param sql
     * @param pageNum
     * @param pageSize
     * @return
     */
    public static String replace_limit(String sql, int pageNum, int pageSize) {
        Objects.requireNonNull(sql);
        Statement statement = getStatement(sql);
        LimitSqlReplaceVisitor visitor = new LimitSqlReplaceVisitor(statement, pageNum, pageSize);
        visitor.parse();
        return statement.toString();
    }

    /**
     * 1、替换sql中的select字段替换为count(*)
     *
     * @param sql
     * @return
     */
    public static String replace_count(String sql) {
        Objects.requireNonNull(sql);
        Statement statement = getStatement(sql);
        CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
        visitor.parse();
        return statement.toString();
    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、替换sql中的select字段替换为count(*)
     * 3、根据分页参数在sql中加入limit
     * <p>
     * <p>
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(:paramList)
     * <p>
     * 性能表现:
     * 其中主要耗时在{@link CCJSqlParserUtil#parse(String)}
     * 所以针对解析sql做了缓存处理{@link #getStatement(String)}
     *
     * @param sql
     * @param paramMap 不会改变
     * @param count    是否开启count(*)替换
     * @param pageNum  null代表不开启limit offset添加
     * @param pageSize null代表不开启limit offset添加
     * @return 根据paramMap生成的新sql
     */
    public static SqlMapResult replace_nullParam_count_limit(String sql, Map<String, Object> paramMap, Boolean count, Integer pageNum, Integer pageSize) {
        Objects.requireNonNull(sql);
        Objects.requireNonNull(paramMap);

        Statement statement = null;
        //如果count不为null、则替换为select count(*)
        if (count != null && count) {
            statement = getStatement(sql);
            CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
            visitor.parse();
        }
        //如果pageNum不为null且pageSize不为null、则加上limit offset
        if (pageNum != null && pageSize != null) {
            if (statement == null) {
                statement = getStatement(sql);
            }
            LimitSqlReplaceVisitor visitor = new LimitSqlReplaceVisitor(statement, pageNum, pageSize);
            visitor.parse();
        }

        Map<String, Object> newParamMap = new LinkedHashMap<>();
        if (!paramMap.isEmpty()) {
            //循环参数map
            //检查是否含有Null元素已经其中List、Array中是否有null
            boolean anyNull = paramMap.values().stream().anyMatch(e -> {
                if (e == null) {
                    return true;
                } else {
                    if (e instanceof List) {
                        return ((List<Object>) e).stream().anyMatch(Objects::isNull);
                    } else if (e.getClass().isArray()) {
                        int len = Array.getLength(e);
                        for (int i = 0; i <= len - 1; i++) {
                            Object val = Array.get(e, i);
                            if (val == null) {
                                return true;
                            }
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
            });
            //如果全部不为Null,直接返回
            if (anyNull) {
                if (statement == null) {
                    statement = getStatement(sql);
                }
                NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(statement, paramMap);
                visitor.parse();
                newParamMap = visitor.newParamMap;
            } else {
                newParamMap.putAll(paramMap);
            }
        }
        //如果处理完了、检测是否有解析过、null则说明没有过任何解析、直接返回sql
        if (statement == null) {
            return new SqlMapResult(sql, newParamMap);
        } else {
            return new SqlMapResult(statement.toString(), newParamMap);
        }

    }

    /**
     * 1、根据传入的参数替换sql中的条件,将参数为null的条件变为 1=1 或者 1=0
     * 2、替换sql中的select字段替换为count(*)
     * 3、根据分页参数在sql中加入limit
     * <p>
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(?,?,?)
     * <p>
     * 性能表现:
     * 以main方法例子{@link #main(String[])}
     * cpu: Intel(R) Core(TM) i5-7360U CPU @ 2.30GHz
     * 单线程、测试10w次，耗时6903ms、约每条耗时0.069ms
     * 其中主要耗时在{@link CCJSqlParserUtil#parse(String)}、约耗时5425ms
     *
     * @param sql
     * @param count    是否开启count(*)替换
     * @param pageNum  null代表不开启limit offset添加
     * @param pageSize null代表不开启limit offset添加
     * @param params   不会改变
     * @return
     * @see SqlListResult#sql  格式化后的sql
     * @see SqlListResult#paramList  去除Null后的paramList,总是生成新的ArrayList
     */
    public static SqlListResult replace_nullParam_count_limit(String sql, boolean count, Integer pageNum, Integer pageSize, Object... params) {
        Objects.requireNonNull(sql);
        Objects.requireNonNull(params);

        Statement statement = null;
        //如果count不为null、则替换为select count(*)
        if (count) {
            statement = getStatement(sql);
            CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
            visitor.parse();
        }
        //如果pageNum不为null且pageSize不为null、则加上limit offset
        if (pageNum != null && pageSize != null) {
            if (statement == null) {
                statement = getStatement(sql);
            }
            LimitSqlReplaceVisitor visitor = new LimitSqlReplaceVisitor(statement, pageNum, pageSize);
            visitor.parse();
        }

        //最后替换null条件
        List<Object> newParamList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>(Arrays.asList(params));
        if (!paramList.isEmpty()) {
            //判断参数集合是否有Null元素
            boolean anyNull = paramList.stream().anyMatch(Objects::isNull);
            //如果有Null,则处理替换
            if (anyNull) {
                if (statement == null) {
                    statement = getStatement(sql);
                }
                NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(statement, paramList);
                visitor.parse();
                //设置新的参数集合
                newParamList = visitor.newParamList;
            } else {
                newParamList = new ArrayList<>(paramList);
            }
        }

        //如果处理完了、检测是否有解析过、null则说明没有过任何解析、直接返回sql
        if (statement == null) {
            return new SqlListResult(sql, newParamList);
        } else {
            return new SqlListResult(statement.toString(), newParamList);
        }
    }

    /**
     * 分页查询
     *
     * @param sql      查询结果集sql(不带limit)
     * @param pageable 分页对象参数
     * @param params   参数
     * @return
     */
    public static <T> Page<T> pageBySql(String sql, Pageable pageable, Class<T> clazz, JdbcTemplate jdbcTemplate, Object... params) {
        SqlListResult countSql = SqlUtil.replace_nullParam_count(sql, params);
        Integer count;
        if (countSql.paramList.isEmpty()) {
            count = jdbcTemplate.queryForObject(countSql.sql, Integer.class);
        } else {
            count = jdbcTemplate.queryForObject(countSql.sql, Integer.class, countSql.paramList.toArray(new Object[0]));
        }
        if (count == null || count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } else {
            SqlListResult dataSql = SqlUtil.replace_nullParam_limit(sql, pageable.getPageNumber(), pageable.getPageSize(), params);
            List<T> dataList;
            if (dataSql.paramList.isEmpty()) {
                dataList = jdbcTemplate.query(dataSql.sql, new BeanPropertyRowMapper<>(clazz));
            } else {
                dataList = jdbcTemplate.query(dataSql.sql, new BeanPropertyRowMapper<>(clazz), dataSql.paramList.toArray(new Object[0]));
            }
            return new PageImpl<>(dataList, pageable, count);
        }
    }

    public record InsertSqlResult<T>(String sql, Class<T> clazz, Field[] insertFields) {
        public int[] insertBatch(final List<T> dataList, final JdbcTemplate jdbcTemplate) {
            final List<Object[]> paramList = new ArrayList<>();
            try {
                for (T t : dataList) {
                    final Object[] param = new Object[insertFields.length];
                    for (int i = 0; i < insertFields.length; i++) {
                        param[i] = (insertFields[i].get(t));
                    }
                    paramList.add(param);
                }
            } catch (IllegalAccessException ex) {
                throw BaseRuntimeException.getException(ex);
            }
            return jdbcTemplate.batchUpdate(sql, paramList);
        }
    }

    public record UpdateSqlResult<T>(String sql, Class<T> clazz, Field[] updateFields, Field[] whereFields) {
        public int[] updateBatch(final List<T> dataList, final JdbcTemplate jdbcTemplate) {
            final List<Object[]> paramList = new ArrayList<>();
            final int fieldsLength = updateFields.length;
            final int whereFieldsLength = whereFields.length;
            try {
                for (T t : dataList) {
                    final Object[] param = new Object[fieldsLength + whereFieldsLength];
                    for (int i = 0; i < fieldsLength; i++) {
                        param[i] = (updateFields[i].get(t));
                    }
                    for (int i = 0; i < whereFieldsLength; i++) {
                        param[i + fieldsLength] = (whereFields[i].get(t));
                    }
                    paramList.add(param);
                }
            } catch (IllegalAccessException ex) {
                throw BaseRuntimeException.getException(ex);
            }
            return jdbcTemplate.batchUpdate(sql, paramList);
        }
    }


    /**
     * 将class转换为insert sql语句对象
     * 会获取父类的字段
     * insert字段会去除掉静态字段
     *
     * @param clazz                实体类class
     * @param table                表名
     * @param fieldFilter          字段名过滤器、false则排除掉、会应用于insert字段
     * @param camelCaseToUnderline 生成的sql中、列名是否是由 字段名驼峰格式转下划线格式而来、会应用于insert字段
     * @param <T>
     * @return
     */
    public static <T> InsertSqlResult<T> toInsertSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter, boolean camelCaseToUnderline) {
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        final List<Field> insertFieldList = new ArrayList<>();
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && fieldFilter.apply(field)) {
                insertFieldList.add(field);
            }
        }
        final StringJoiner sj1 = new StringJoiner(",");
        final StringJoiner sj2 = new StringJoiner(",");
        for (Field field : insertFieldList) {
            field.setAccessible(true);
            if (camelCaseToUnderline) {
                sj1.add(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sj1.add(field.getName());
            }
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
        return new InsertSqlResult<>(sb.toString(), clazz, insertFieldList.toArray(new Field[0]));
    }

    /**
     * 将class转换为update sql语句对象
     * 会获取父类的所有字段
     * update字段会去除掉静态字段
     *
     * @param clazz                实体类
     * @param table                表名
     * @param fieldFilter          字段名过滤器、false则排除掉、会应用于update字段
     * @param camelCaseToUnderline 生成的sql中、列名是否是由 字段名驼峰格式转下划线格式而来、会应用于update字段和where字段
     * @param whereFieldNames      where字段名
     * @param <T>
     * @return
     */
    public static <T> UpdateSqlResult<T> toUpdateSqlResult(Class<T> clazz, String table, Function<Field, Boolean> fieldFilter, boolean camelCaseToUnderline, String... whereFieldNames) {
        final Field[] allFields = FieldUtils.getAllFields(clazz);
        final List<Field> updateFieldList = new ArrayList<>();
        final Map<String, Field> whereMap = new HashMap<>();
        final Set<String> whereColumnSet = Set.of(whereFieldNames);
        for (Field field : allFields) {
            if (!Modifier.isStatic(field.getModifiers()) && fieldFilter.apply(field)) {
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
            if (camelCaseToUnderline) {
                sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sb.append(field.getName());
            }
            sb.append("=?");
        }
        sb.append(" where ");
        for (int i = 0; i < whereFieldList.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            final Field field = whereFieldList.get(i);
            field.setAccessible(true);
            if (camelCaseToUnderline) {
                sb.append(StringUtil.camelCaseToSplitChar(field.getName(), '_'));
            } else {
                sb.append(field.getName());
            }
            sb.append("=?");
        }
        return new UpdateSqlResult<>(sb.toString(), clazz, updateFieldList.toArray(new Field[0]), whereFieldList.toArray(new Field[0]));
    }

}
