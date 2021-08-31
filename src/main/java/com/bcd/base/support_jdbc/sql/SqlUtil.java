package com.bcd.base.support_jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SqlUtil {

    static Logger logger = LoggerFactory.getLogger(SqlUtil.class);

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
        SqlListResult sqlListResult = replace_nullParam(sql1, paramList1.toArray());
        System.out.println(sqlListResult.getSql());
        sqlListResult.getParamList().forEach(e -> System.out.print(e + "    "));


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
        System.out.println(sqlMapResult.getSql());
        sqlMapResult.getParamMap().forEach((k, v) -> {
            if (v.getClass().isArray()) {
                Object[] arr = (Object[]) v;
                System.out.print(k + ":" + Arrays.asList(arr) + "    ");
            } else {
                System.out.print(k + ":" + v + "    ");
            }
        });

//        long t1=System.currentTimeMillis();
//        for(int i=1;i<=100000;i++){
//            replace_nullParam_count_limit(sql1,true,1,20, paramList1.toArray());
//        }
//        long t2=System.currentTimeMillis();
//        for(int i=1;i<=100000;i++){
//            replace_nullParam_count_limit(sql2,true,1,20, paramMap2);
//        }
//        long t3=System.currentTimeMillis();
//        for(int i=1;i<=100000;i++){
//            CCJSqlParserUtil.parse(sql1);
//        }
//        long t4=System.currentTimeMillis();
//        System.out.printf("\n%dms,%dms,%dms",t2-t1,t3-t2,t4-t3);
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
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            LimitSqlReplaceVisitor visitor = new LimitSqlReplaceVisitor(statement, pageNum, pageSize);
            visitor.parse();
            return statement.toString();
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 1、替换sql中的select字段替换为count(*)
     *
     * @param sql
     * @return
     */
    public static String replace_count(String sql) {
        Objects.requireNonNull(sql);
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
            visitor.parse();
            return statement.toString();
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
        }
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
     * 以main方法例子{@link #main(String[])}
     * cpu: Intel(R) Core(TM) i5-7360U CPU @ 2.30GHz
     * 单线程、测试10w次、耗时6555ms、约每条耗时0.066ms
     * 其中主要耗时在{@link CCJSqlParserUtil#parse(String)}、约耗时5425ms
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
        try {
            Statement statement = null;
            //如果count不为null、则替换为select count(*)
            if (count != null && count) {
                statement = CCJSqlParserUtil.parse(sql);
                CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
                visitor.parse();
            }
            //如果pageNum不为null且pageSize不为null、则加上limit offset
            if (pageNum != null && pageSize != null) {
                if (statement == null) {
                    statement = CCJSqlParserUtil.parse(sql);
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
                        statement = CCJSqlParserUtil.parse(sql);
                    }
                    NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(statement, paramMap);
                    visitor.parse();
                    newParamMap = visitor.getNewParamMap();
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
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
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
     * @see SqlListResult#getSql()  格式化后的sql
     * @see SqlListResult#getParamList()  去除Null后的paramList,总是生成新的ArrayList
     */
    public static SqlListResult replace_nullParam_count_limit(String sql, Boolean count, Integer pageNum, Integer pageSize, Object... params) {
        Objects.requireNonNull(sql);
        Objects.requireNonNull(params);

        try {
            Statement statement = null;
            //如果count不为null、则替换为select count(*)
            if (count != null && count) {
                statement = CCJSqlParserUtil.parse(sql);
                CountSqlReplaceVisitor visitor = new CountSqlReplaceVisitor(statement);
                visitor.parse();
            }
            //如果pageNum不为null且pageSize不为null、则加上limit offset
            if (pageNum != null && pageSize != null) {
                if (statement == null) {
                    statement = CCJSqlParserUtil.parse(sql);
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
                        statement = CCJSqlParserUtil.parse(sql);
                    }
                    NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(statement, paramList);
                    visitor.parse();
                    //设置新的参数集合
                    newParamList = visitor.getNewParamList();
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
        } catch (JSQLParserException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 生成insert sql
     *
     * @param columnSet
     * @param table
     * @return
     */
    private static String generateCreateSql(Set<String> columnSet, String table) {
        StringBuilder sql = new StringBuilder("insert into ");
        sql.append(table);
        sql.append("(");
        sql.append(columnSet.stream().reduce((e1, e2) -> e1 + "," + e2).get());
        sql.append(") values(");
        sql.append(columnSet.stream().map(e -> "?").reduce((e1, e2) -> e1 + "," + e2).get());
        sql.append(")");
        return sql.toString();
    }

    /**
     * 生成update sql
     *
     * @param columnSet
     * @param table
     * @return
     */
    private static String generateUpdateSql(Set<String> columnSet, Set<String> whereColumnSet, String table) {
        StringBuilder sql = new StringBuilder("update ");
        sql.append(table);
        sql.append(" set ");
        boolean[] isFirst = new boolean[]{true};
        columnSet.forEach(e -> {
            if (isFirst[0]) {
                isFirst[0] = false;
            } else {
                sql.append(",");
            }
            sql.append(e);
            sql.append("=?");
        });
        if (whereColumnSet != null && !whereColumnSet.isEmpty()) {
            isFirst[0] = true;
            sql.append(" where ");
            whereColumnSet.forEach(e -> {
                if (isFirst[0]) {
                    isFirst[0] = false;
                } else {
                    sql.append(" and ");
                }
                sql.append(e);
                sql.append("=?");
            });
        }
        return sql.toString();
    }


    /**
     * 生成批量中间结果
     * [0]:列名和字段对应map Map<String,Field>
     * [1]:批量参数集合 List<Object[]>
     *
     * @param dataList
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Object[] toBatchResult(List dataList, BiFunction<String, Object, Object> fieldHandler, String... ignoreFields) {
        Object first = dataList.get(0);
        Class clazz = first.getClass();
        Map<String, Field> columnToFieldMap = new LinkedHashMap<>();
        List<Field> fieldList = FieldUtils.getAllFieldsList(clazz);
        Set<String> ignoreSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        fieldList.forEach(field -> {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
            String fieldName = field.getName();
            String columnName = StringUtil.toFirstSplitWithUpperCase(fieldName, '_');
            if (ignoreSet.contains(fieldName) || ignoreSet.contains(columnName)) {
                return;
            }
            field.setAccessible(true);
            columnToFieldMap.put(columnName, field);
        });

        List<Object[]> paramList = (List<Object[]>) dataList.stream().map(data -> {
            List param = new ArrayList();
            columnToFieldMap.forEach((k, v) -> {
                try {
                    Object val = v.get(data);
                    if (fieldHandler != null) {
                        val = fieldHandler.apply(k, val);
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            return param.toArray();
        }).collect(Collectors.toList());

        return new Object[]{columnToFieldMap, paramList};
    }

    /**
     * 生成批量新增结果
     *
     * @param dataList     数据集合
     * @param table        表名
     * @param fieldHandler 字段值二次处理器
     * @param ignoreFields 忽略的新增字段
     * @return
     */
    public static BatchCreateSqlResult generateBatchCreateResult(List dataList, String table, BiFunction<String, Object, Object> fieldHandler, String... ignoreFields) {
        if (dataList.isEmpty()) {
            return null;
        } else {
            Object[] res = toBatchResult(dataList, fieldHandler, ignoreFields);
            Map<String, Field> columnToFieldMap = (Map<String, Field>) res[0];
            List<Object[]> paramList = (List<Object[]>) res[1];
            String sql = generateCreateSql(columnToFieldMap.keySet(), table);
            return new BatchCreateSqlResult(sql, paramList);
        }
    }

    /**
     * 生成批量中间结果
     * [0]:更新列名和字段对应map Map<String,Field>
     * [1]:where条件列名和字段对应map Map<String,Field>(如果存在字段的值为null,会抛出异常)
     * [2]:批量参数集合 List<Object[]>(包括更新字段值和where字段值)
     *
     * @param dataList
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Object[] toBatchResultWithWhere(List dataList, BiFunction<String, Object, Object> fieldHandler, String[] whereFields, String... ignoreFields) {
        Object first = dataList.get(0);
        Class clazz = first.getClass();
        Map<String, Field> columnToFieldMap = new LinkedHashMap<>();
        Map<String, Field> whereColumnToFieldMap = new LinkedHashMap<>();
        List<Field> fieldList = FieldUtils.getAllFieldsList(clazz);
        Set<String> ignoreSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        Set<String> whereSet = Arrays.stream(whereFields).collect(Collectors.toSet());
        fieldList.forEach(field -> {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
            String fieldName = field.getName();
            String columnName = StringUtil.toFirstSplitWithUpperCase(fieldName, '_');
            if (whereSet.contains(fieldName) || whereSet.contains(columnName)) {
                field.setAccessible(true);
                whereColumnToFieldMap.put(columnName, field);
            }
            if (!(ignoreSet.contains(fieldName) || ignoreSet.contains(columnName))) {
                field.setAccessible(true);
                columnToFieldMap.put(columnName, field);
            }

        });

        List<Object[]> paramList = (List<Object[]>) dataList.stream().map(data -> {
            List param = new ArrayList();
            columnToFieldMap.forEach((k, v) -> {
                //如果where条件中存在此字段,则不作为更新字段
                if (whereColumnToFieldMap.keySet().contains(k)) {
                    return;
                }
                try {
                    Object val = v.get(data);
                    if (fieldHandler != null) {
                        val = fieldHandler.apply(k, val);
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            whereColumnToFieldMap.forEach((k, v) -> {
                try {
                    Object val = v.get(data);
                    if (fieldHandler != null) {
                        val = fieldHandler.apply(k, val);
                    }
                    if (val == null) {
                        throw BaseRuntimeException.getException("where field[" + k + "] can't be null");
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            return param.toArray();
        }).collect(Collectors.toList());
        return new Object[]{columnToFieldMap, whereColumnToFieldMap, paramList};
    }

    /**
     * 生成批量更新结果
     *
     * @param dataList     数据集合
     * @param table        表名
     * @param fieldHandler 字段值二次处理器
     * @param whereFields  作为where条件的字段(若字段值为null,则忽略条件;如果所有的字段值都为null,则异常)
     * @param ignoreFields 忽略的更新字段
     * @return
     */
    public static BatchUpdateSqlResult generateBatchUpdateResult(List dataList, String table, BiFunction<String, Object, Object> fieldHandler, String[] whereFields, String... ignoreFields) {
        if (dataList.isEmpty()) {
            return null;
        } else {
            Object[] res = toBatchResultWithWhere(dataList, fieldHandler, whereFields, ignoreFields);
            Map<String, Field> columnToFieldMap = (Map<String, Field>) res[0];
            Map<String, Field> whereColumnToFieldMap = (Map<String, Field>) res[1];
            List<Object[]> paramList = (List<Object[]>) res[2];
            String sql = generateUpdateSql(columnToFieldMap.keySet(), whereColumnToFieldMap.keySet(), table);
            return new BatchUpdateSqlResult(sql, paramList);
        }
    }

    /**
     * 生成字段和值对应map
     *
     * @param obj
     * @param includeNullValue
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Map<String, Object> toColumnValueMap(Object obj, boolean includeNullValue, BiFunction<String, Object, Object> fieldHandler, String... ignoreFields) {
        Map<String, Object> returnMap = new LinkedHashMap<>();
        List<Field> fieldList = FieldUtils.getAllFieldsList(obj.getClass());
        Set<String> ignoreSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        fieldList.forEach(field -> {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
            String fieldName = field.getName();
            String columnName = StringUtil.toFirstSplitWithUpperCase(fieldName, '_');
            if (ignoreSet.contains(fieldName) || ignoreSet.contains(columnName)) {
                return;
            }
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                if (!includeNullValue && val == null) {
                    return;
                }
                if (fieldHandler != null) {
                    val = fieldHandler.apply(fieldName, val);
                }
                returnMap.put(columnName, val);
            } catch (IllegalAccessException e) {
                throw BaseRuntimeException.getException(e);
            }
        });
        return returnMap;
    }

    /**
     * @param obj              对象
     * @param table            表名
     * @param includeNullValue 是否包含空值的列
     * @param ignoreFields     忽视的字段和者列名
     * @param fieldHandler     字段处理器,用于处理字段值
     * @return
     */
    public static CreateSqlResult generateCreateResult(Object obj, String table, boolean includeNullValue, BiFunction<String, Object, Object> fieldHandler, String... ignoreFields) {
        Map<String, Object> filterColumnValueMap = toColumnValueMap(obj, includeNullValue, fieldHandler, ignoreFields);
        if (filterColumnValueMap.size() == 0) {
            throw BaseRuntimeException.getException("no column");
        }
        String sql = generateCreateSql(filterColumnValueMap.keySet(), table);
        return new CreateSqlResult(sql, new ArrayList(filterColumnValueMap.values()));
    }

    /**
     * @param obj              对象
     * @param table            表名
     * @param includeNullValue 是否包含空值的列
     * @param ignoreFields     忽视的字段和者列名
     * @param fieldHandler     字段处理器,用于处理字段值
     * @return
     */
    public static UpdateSqlResult generateUpdateResult(Object obj, String table, boolean includeNullValue, BiFunction<String, Object, Object> fieldHandler, String... ignoreFields) {
        Map<String, Object> filterColumnValueMap = toColumnValueMap(obj, includeNullValue, fieldHandler, ignoreFields);
        if (filterColumnValueMap.size() == 0) {
            throw BaseRuntimeException.getException("no column");
        }
        String sql = generateUpdateSql(filterColumnValueMap.keySet(), null, table);
        return new UpdateSqlResult(sql, new ArrayList(filterColumnValueMap.values()));
    }

}
