package com.bcd.mongodb.util;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.*;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.mongodb.condition.converter.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public class ConditionUtil {
    private final static Map<Class,Converter> CONDITION_CONVERTER_MAP=new HashMap<>();
    static{
        CONDITION_CONVERTER_MAP.put(ConditionImpl.class,new ConditionImplConverter());
        CONDITION_CONVERTER_MAP.put(DateCondition.class,new DateConditionConverter());
        CONDITION_CONVERTER_MAP.put(NullCondition.class,new NullConditionConverter());
        CONDITION_CONVERTER_MAP.put(NumberCondition.class,new NumberConditionConverter());
        CONDITION_CONVERTER_MAP.put(StringCondition.class,new StringConditionConverter());
        CONDITION_CONVERTER_MAP.put(BooleanCondition.class,new BooleanConditionConverter());
    }

    public static Query toQuery(Condition condition){
        Criteria criteria= convertCondition(condition);
        if(criteria==null){
            return new Query();
        }
        return new Query(criteria);
    }

    public static Criteria convertCondition(Condition condition){
        Converter converter=CONDITION_CONVERTER_MAP.get(condition.getClass());
        if(converter==null){
            throw BaseRuntimeException.getException("[ConditionUtil.convertCondition],Condition["+condition.getClass()+"] Have Not Converter!");
        }else{
            return (Criteria)converter.convert(condition);
        }
    }
}
