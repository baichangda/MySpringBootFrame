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
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)->{
            Predicate predicate=convertCondition(condition,root,query,cb);
            if(predicate==null){
                return cb.and();
            }else{
                return predicate;
            }
        };
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
}
