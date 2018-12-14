package com.bcd.rdb.jdbc.sql;

import com.bcd.base.exception.BaseRuntimeException;
import net.sf.jsqlparser.JSQLParserException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SqlUtil {

    public static void main(String [] args) throws JSQLParserException {
        String sql1="SELECT \n" +
                "    *\n" +
                "FROM\n" +
                "    t_sys_user a\n" +
                "        INNER JOIN\n" +
                "    t_sys_user_role b ON a.id = b.user_id\n" +
                "WHERE\n" +
                "    username LIKE ? AND sex = ? AND status=? AND type in (?,?,?) AND phone in (?,?)";
        List<Object> paramList1=new ArrayList<>();
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
        SqlListResult sqlListResult=replaceNull(sql1,paramList1);
        System.out.println(sqlListResult.getSql());
        sqlListResult.getParamList().forEach(e->System.out.print(e+"    "));


        System.out.println();
        String sql2="SELECT \n" +
                "    *\n" +
                "FROM\n" +
                "    t_sys_user a\n" +
                "        INNER JOIN\n" +
                "    t_sys_user_role b ON a.id = b.user_id\n" +
                "WHERE\n" +
                "    username LIKE :username AND sex = :sex AND status=:status And type in (:type) And phone in (:phone)";
        Map<String,Object> paramMap2=new LinkedHashMap<>();
        paramMap2.put("username","%z%");
        paramMap2.put("sex",1);
        paramMap2.put("status",null);
        paramMap2.put("type",Arrays.asList(1,null,3));
        paramMap2.put("phone",new Object[]{1,null});
        SqlMapResult sqlMapResult=replaceNull(sql2,paramMap2);
        System.out.println(sqlMapResult.getSql());
        sqlMapResult.getParamMap().forEach((k,v)->{
            if(v.getClass().isArray()){
                Object[] arr=(Object[])v;
                System.out.print(k+":"+Arrays.asList(arr)+"    ");
            }else {
                System.out.print(k + ":" + v + "    ");
            }
        });
    }


    /**
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(:paramList)
     * @param sql
     * @param paramMap 不会改变
     * @return 根据paramMap生成的新sql
     */
    public static SqlMapResult replaceNull(String sql, Map<String,Object> paramMap){
        if(sql==null){
            throw BaseRuntimeException.getException("Param[sql] Can Not Be Null");
        }
        if(paramMap==null){
            throw BaseRuntimeException.getException("Param[paramMap] Can Not Be Null");
        }
        if(paramMap.size()==0){
            return new SqlMapResult(sql,new LinkedHashMap<>());
        }
        //1、定义新的paramMap
        Map<String,Object> newParamMap=new LinkedHashMap<>();
        //2、循环参数map
        //去除Null元素
        //去除val为List类型且为empty的元素,List中的Null元素也会被移除
        paramMap.forEach((k,v)->{
            if(v!=null){
                if(v instanceof List){
                    List<Object> validList = ((List<Object>) v).stream().filter(e -> e != null).collect(Collectors.toList());
                    if(!validList.isEmpty()){
                        newParamMap.put(k,validList);
                    }
                }else if(v.getClass().isArray()){
                    List<Object> validList=new ArrayList<>();
                    int len=Array.getLength(v);
                    for(int i=0;i<=len-1;i++){
                        Object val= Array.get(v,i);
                        if(val!=null){
                            validList.add(val);
                        }
                    }
                    if(validList.size()>0){
                        newParamMap.put(k,validList.toArray());
                    }
                }else{
                    newParamMap.put(k,v);
                }
            }
        });
        //3、用格式化后的参数生成sql
        NullParamSqlReplaceVisitor visitor= new NullParamSqlReplaceVisitor(sql,newParamMap);
        String newSql=visitor.parseSql();
        return new SqlMapResult(newSql,newParamMap);
    }


    /**
     * 支持的操作符有 = >  <  >=  <=  <>  like  in(?,?,?)
     * @param sql
     * @param paramList 不会改变
     * @return
     * @see SqlListResult#sql 格式化后的sql
     * @see SqlListResult#paramList 去除Null后的paramList,总是生成新的ArrayList
     */
    public static SqlListResult replaceNull(String sql, List<Object> paramList){
        if(sql==null){
            throw BaseRuntimeException.getException("Param[sql] Can Not Be Null");
        }
        if(paramList==null){
            throw BaseRuntimeException.getException("Param[paramList] Can Not Be Null");
        }
        if(paramList.size()==0){
            return new SqlListResult(sql,new ArrayList<>());
        }
        //1、判断参数集合是否有Null元素
        boolean hasNull=paramList.stream().anyMatch(e->e==null);
        //1.1、如果全部不为Null,直接返回
        if(!hasNull){
            return new SqlListResult(sql,new ArrayList<>(paramList));
        }
        //2、用参数集合生成新sql
        NullParamSqlReplaceVisitor visitor= new NullParamSqlReplaceVisitor(sql,paramList);
        String newSql=visitor.parseSql();
        //3、返回新sql和移除掉Null参数的新集合
        return new SqlListResult(newSql,paramList.stream().filter(e->e!=null).collect(Collectors.toList()));
    }


}
