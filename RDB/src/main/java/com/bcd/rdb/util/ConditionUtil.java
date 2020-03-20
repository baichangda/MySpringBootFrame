package com.bcd.rdb.util;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.*;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.condition.converter.jpa.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public class ConditionUtil {
    private final static Map<Class,Converter> JPA_CONDITION_CONVERTER_MAP=new HashMap<>();
    static{
        JPA_CONDITION_CONVERTER_MAP.put(ConditionImpl.class,new ConditionImplConverter());
        JPA_CONDITION_CONVERTER_MAP.put(DateCondition.class,new DateConditionConverter());
        JPA_CONDITION_CONVERTER_MAP.put(NullCondition.class,new NullConditionConverter());
        JPA_CONDITION_CONVERTER_MAP.put(NumberCondition.class,new NumberConditionConverter());
        JPA_CONDITION_CONVERTER_MAP.put(StringCondition.class,new StringConditionConverter());
        JPA_CONDITION_CONVERTER_MAP.put(BooleanCondition.class,new BooleanConditionConverter());
    }

    private final static Map<Class,Converter> JDBC_CONDITION_CONVERTER_MAP=new HashMap<>();
    static{
        JDBC_CONDITION_CONVERTER_MAP.put(ConditionImpl.class,new com.bcd.rdb.condition.converter.jdbc.ConditionImplConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(DateCondition.class,new com.bcd.rdb.condition.converter.jdbc.DateConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(NullCondition.class,new com.bcd.rdb.condition.converter.jdbc.NullConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(NumberCondition.class,new com.bcd.rdb.condition.converter.jdbc.NumberConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(StringCondition.class,new com.bcd.rdb.condition.converter.jdbc.StringConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(BooleanCondition.class,new com.bcd.rdb.condition.converter.jdbc.BooleanConditionConverter());
    }



    public static <T>Path parseRootPath(Root<T> root, String attrName){
        Path path=null;
        if(attrName.indexOf('.')!=-1){
            String [] attrArr=attrName.split("\\.");
            for(int i=0;i<=attrArr.length-1;i++){
                if(path==null){
                    path=root.get(attrArr[i]);
                }else{
                    path=path.get(attrArr[i]);
                }
            }
        }else{
            path=root.get(attrName);
        }
        return path;
    }

    public static <T>Specification<T> toSpecification(Condition condition){
        Specification<T> specification=(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)->{
            Predicate predicate=convertCondition(condition,root,query,cb);
            if(predicate==null){
                return cb.and();
            }else{
                return predicate;
            }
        };
        return specification;
    }

    public static Predicate convertCondition(Condition condition,Root root, CriteriaQuery query, CriteriaBuilder cb){
        if(condition==null){
            return null;
        }
        Converter converter=JPA_CONDITION_CONVERTER_MAP.get(condition.getClass());
        if(converter==null){
            throw BaseRuntimeException.getException("[ConditionUtil.convertCondition],Condition["+condition.getClass()+"] Have Not Converter!");
        }else{
            return (Predicate)converter.convert(condition,root,query,cb);
        }
    }

    /**
     * 转换condition为 jdbc where条件
     * @param condition
     * @param paramMap
     * @param deep 层深(用于格式化where sql),最外层调用传1
     * @return
     */
    public static String convertCondition(Condition condition,Map<String,Object> paramMap,Map<String,Integer> paramToCount,int deep){
        if(condition==null){
            return null;
        }
        Converter converter=JDBC_CONDITION_CONVERTER_MAP.get(condition.getClass());
        if(converter==null){
            throw BaseRuntimeException.getException("[ConditionUtil.convertCondition],Condition["+condition.getClass()+"] Have Not Converter!");
        }else{
            return (String)converter.convert(condition,paramMap,paramToCount,deep);
        }
    }

    public static String convertCondition(Condition condition,Map<String,Object> paramMap){
        return convertCondition(condition,paramMap,new HashMap<>(),1);
    }
}
