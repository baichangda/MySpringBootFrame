package com.bcd.rdb.jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
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

    static Logger logger= LoggerFactory.getLogger(SqlUtil.class);

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
        paramList1.add("%z%");
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
        SqlListResult sqlListResult = replaceNull(sql1, paramList1.toArray());
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
        SqlMapResult sqlMapResult = replaceNull(sql2, paramMap2);
        System.out.println(sqlMapResult.getSql());
        sqlMapResult.getParamMap().forEach((k, v) -> {
            if (v.getClass().isArray()) {
                Object[] arr = (Object[]) v;
                System.out.print(k + ":" + Arrays.asList(arr) + "    ");
            } else {
                System.out.print(k + ":" + v + "    ");
            }
        });


        Statement statement= CCJSqlParserUtil.parse(sql1);


        long t1=System.currentTimeMillis();
        for(int i=1;i<=100000;i++){
            replaceNull(sql1, paramList1.toArray());
        }
        long t2=System.currentTimeMillis();
        for(int i=1;i<=100000;i++){
            replaceNull(sql2, paramMap2);
        }
        long t3=System.currentTimeMillis();
        System.out.println(t2-t1);
        System.out.println(t3-t2);
    }


    /**
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(:paramList)
     *
     * 性能测试:
     * 采用main方法中例子测试10w次，耗时7638ms
     *
     * @param sql
     * @param paramMap 不会改变
     * @return 根据paramMap生成的新sql
     */
    public static SqlMapResult replaceNull(String sql, Map<String, Object> paramMap) {
        if (sql == null) {
            throw BaseRuntimeException.getException("Param[sql] Can Not Be Null");
        }
        if (paramMap == null) {
            throw BaseRuntimeException.getException("Param[paramMap] Can Not Be Null");
        }
        if (paramMap.size() == 0) {
            return new SqlMapResult(sql, new LinkedHashMap<>());
        }
        //1、定义新的paramMap
        Map<String, Object> newParamMap = new LinkedHashMap<>();
        //2、循环参数map
        //去除Null元素
        //去除val为List或Array类型且为空的元素,List或者Array中的Null元素也会被移除
        paramMap.forEach((k, v) -> {
            if (v != null) {
                if (v instanceof List) {
                    List<Object> validList = ((List<Object>) v).stream().filter(Objects::nonNull).collect(Collectors.toList());
                    if (!validList.isEmpty()) {
                        newParamMap.put(k, validList);
                    }
                } else if (v.getClass().isArray()) {
                    List<Object> validList = new ArrayList<>();
                    int len = Array.getLength(v);
                    for (int i = 0; i <= len - 1; i++) {
                        Object val = Array.get(v, i);
                        if (val != null) {
                            validList.add(val);
                        }
                    }
                    if (!validList.isEmpty()) {
                        newParamMap.put(k, validList.toArray());
                    }
                } else {
                    newParamMap.put(k, v);
                }
            }
        });
        //3、用格式化后的参数生成sql
        NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(sql, newParamMap);
        String newSql = visitor.parseSql();
        return new SqlMapResult(newSql, newParamMap);
    }

    /**
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(?,?,?)
     *
     * 性能测试:
     * 采用main方法中例子测试10w次，耗时7080ms
     *
     * @param sql
     * @param params 不会改变
     * @return
     * @see SqlListResult#getSql()  格式化后的sql
     * @see SqlListResult#getParamList()  去除Null后的paramList,总是生成新的ArrayList
     */
    public static SqlListResult replaceNull(String sql, Object... params) {
        List<Object> paramList = new ArrayList(Arrays.asList(params));
        if (sql == null) {
            throw BaseRuntimeException.getException("Param[sql] Can Not Be Null");
        }
        if (paramList == null) {
            throw BaseRuntimeException.getException("Param[paramList] Can Not Be Null");
        }
        if (paramList.isEmpty()) {
            return new SqlListResult(sql, new ArrayList<>());
        }
        //1、判断参数集合是否有Null元素
        boolean hasNull = paramList.stream().anyMatch(Objects::isNull);
        //1.1、如果全部不为Null,直接返回
        if (!hasNull) {
            return new SqlListResult(sql, new ArrayList<>(paramList));
        }
        //2、用参数集合生成新sql
        NullParamSqlReplaceVisitor visitor = new NullParamSqlReplaceVisitor(sql, paramList);
        String newSql = visitor.parseSql();
        //3、返回新sql和移除掉Null参数的新集合
        return new SqlListResult(newSql, paramList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * 生成create sql
     * @param columnSet
     * @param table
     * @return
     */
    private static String generateCreateSql(Set<String> columnSet,String table){
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
     * @param columnSet
     * @param table
     * @return
     */
    private static String generateUpdateSql(Set<String> columnSet,Set<String> whereColumnSet,String table){
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
        if(whereColumnSet!=null&&!whereColumnSet.isEmpty()) {
            isFirst[0]=true;
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
     * @param dataList
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Object[] toBatchResult(List dataList, BiFunction<String,Object,Object> fieldHandler, String... ignoreFields){
        Object first= dataList.get(0);
        Class clazz=first.getClass();
        Map<String,Field> columnToFieldMap=new LinkedHashMap<>();
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
            columnToFieldMap.put(columnName,field);
        });

        List<Object[]> paramList= (List<Object[]>)dataList.stream().map(data->{
            List param=new ArrayList();
            columnToFieldMap.forEach((k,v)->{
                try {
                    Object val= v.get(data);
                    if(fieldHandler!=null){
                        val=fieldHandler.apply(k,val);
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            return param.toArray();
        }).collect(Collectors.toList());

        return new Object[]{columnToFieldMap,paramList};
    }

    /**
     * 生成批量新增结果
     * @param dataList 数据集合
     * @param table 表名
     * @param fieldHandler 字段值二次处理器
     * @param ignoreFields 忽略的新增字段
     * @return
     */
    public static BatchCreateSqlResult generateBatchCreateResult(List dataList, String table, BiFunction<String,Object,Object> fieldHandler, String... ignoreFields) {
        if(dataList.isEmpty()){
            return null;
        }else{
           Object[] res= toBatchResult(dataList, fieldHandler, ignoreFields);
           Map<String,Field> columnToFieldMap=(Map<String,Field>)res[0];
           List<Object[]> paramList=(List<Object[]>)res[1];
           String sql= generateCreateSql(columnToFieldMap.keySet(),table);
           return new BatchCreateSqlResult(sql,paramList);
        }
    }

    /**
     * 生成批量中间结果
     * [0]:更新列名和字段对应map Map<String,Field>
     * [1]:where条件列名和字段对应map Map<String,Field>(如果存在字段的值为null,会抛出异常)
     * [2]:批量参数集合 List<Object[]>(包括更新字段值和where字段值)
     * @param dataList
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Object[] toBatchResultWithWhere(List dataList, BiFunction<String,Object,Object> fieldHandler,String[] whereFields, String... ignoreFields){
        Object first= dataList.get(0);
        Class clazz=first.getClass();
        Map<String,Field> columnToFieldMap=new LinkedHashMap<>();
        Map<String,Field> whereColumnToFieldMap=new LinkedHashMap<>();
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
                whereColumnToFieldMap.put(columnName,field);
            }
            if (!(ignoreSet.contains(fieldName) || ignoreSet.contains(columnName))) {
                field.setAccessible(true);
                columnToFieldMap.put(columnName,field);
            }

        });

        List<Object[]> paramList= (List<Object[]>)dataList.stream().map(data->{
            List param=new ArrayList();
            columnToFieldMap.forEach((k,v)->{
                //如果where条件中存在此字段,则不作为更新字段
                if(whereColumnToFieldMap.keySet().contains(k)){
                    return;
                }
                try {
                    Object val= v.get(data);
                    if(fieldHandler!=null){
                        val=fieldHandler.apply(k,val);
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            whereColumnToFieldMap.forEach((k,v)->{
                try {
                    Object val= v.get(data);
                    if(fieldHandler!=null){
                        val=fieldHandler.apply(k,val);
                    }
                    if(val==null){
                        throw BaseRuntimeException.getException("where field["+k+"] can't be null");
                    }
                    param.add(val);
                } catch (IllegalAccessException ex) {
                    throw BaseRuntimeException.getException(ex);
                }
            });
            return param.toArray();
        }).collect(Collectors.toList());
        return new Object[]{columnToFieldMap,whereColumnToFieldMap,paramList};
    }

    /**
     * 生成批量更新结果
     * @param dataList 数据集合
     * @param table 表名
     * @param fieldHandler 字段值二次处理器
     * @param whereFields 作为where条件的字段(若字段值为null,则忽略条件;如果所有的字段值都为null,则异常)
     * @param ignoreFields 忽略的更新字段
     * @return
     */
    public static BatchUpdateSqlResult generateBatchUpdateResult(List dataList, String table, BiFunction<String,Object,Object> fieldHandler,String[] whereFields, String... ignoreFields) {
        if(dataList.isEmpty()){
            return null;
        }else{
            Object[] res= toBatchResultWithWhere(dataList, fieldHandler,whereFields, ignoreFields);
            Map<String,Field> columnToFieldMap=(Map<String,Field>)res[0];
            Map<String,Field> whereColumnToFieldMap=(Map<String,Field>)res[1];
            List<Object[]> paramList=(List<Object[]>)res[2];
            String sql= generateUpdateSql(columnToFieldMap.keySet(),whereColumnToFieldMap.keySet(),table);
            return new BatchUpdateSqlResult(sql,paramList);
        }
    }

    /**
     * 生成字段和值对应map
     * @param obj
     * @param includeNullValue
     * @param fieldHandler
     * @param ignoreFields
     * @return
     */
    private static Map<String, Object> toColumnValueMap(Object obj, boolean includeNullValue, BiFunction<String,Object,Object> fieldHandler, String... ignoreFields) {
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
                if(fieldHandler!=null){
                    val=fieldHandler.apply(fieldName,val);
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
     * @param fieldHandler 字段处理器,用于处理字段值
     * @return
     */
    public static CreateSqlResult generateCreateResult(Object obj, String table, boolean includeNullValue, BiFunction<String,Object,Object> fieldHandler, String... ignoreFields) {
        Map<String, Object> filterColumnValueMap = toColumnValueMap(obj, includeNullValue,fieldHandler, ignoreFields);
        if (filterColumnValueMap.size() == 0) {
            throw BaseRuntimeException.getException("no column");
        }
        String sql=generateCreateSql(filterColumnValueMap.keySet(),table);
        return new CreateSqlResult(sql, new ArrayList(filterColumnValueMap.values()));
    }

    /**
     * @param obj              对象
     * @param table            表名
     * @param includeNullValue 是否包含空值的列
     * @param ignoreFields     忽视的字段和者列名
     * @param fieldHandler 字段处理器,用于处理字段值
     * @return
     */
    public static UpdateSqlResult generateUpdateResult(Object obj, String table, boolean includeNullValue, BiFunction<String,Object,Object> fieldHandler, String... ignoreFields) {
        Map<String, Object> filterColumnValueMap = toColumnValueMap(obj, includeNullValue,fieldHandler, ignoreFields);
        if (filterColumnValueMap.size() == 0) {
            throw BaseRuntimeException.getException("no column");
        }
        String sql=generateUpdateSql(filterColumnValueMap.keySet(),null,table);
        return new UpdateSqlResult(sql, new ArrayList(filterColumnValueMap.values()));
    }
}
