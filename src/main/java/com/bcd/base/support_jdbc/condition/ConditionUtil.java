package com.bcd.base.support_jdbc.condition;


import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.*;
import com.bcd.base.exception.BaseRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionUtil {

    private final static Map<Class, Converter> JDBC_CONDITION_CONVERTER_MAP = new HashMap<>();

    static {
        JDBC_CONDITION_CONVERTER_MAP.put(NumberCondition.class, new NumberConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(StringCondition.class, new StringConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(DateCondition.class, new DateConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(NullCondition.class, new NullConditionConverter());
        JDBC_CONDITION_CONVERTER_MAP.put(ConcatCondition.class, new ConcatConditionConverter());
    }

    public static ConvertRes convertCondition(Condition condition,BeanInfo beanInfo) {
        if (condition == null) {
            return null;
        }
        Converter converter = JDBC_CONDITION_CONVERTER_MAP.get(condition.getClass());
        if (converter == null) {
            throw BaseRuntimeException.getException("[ConditionUtil.convertCondition],Condition[" + condition.getClass() + "] Have Not Converter!");
        } else {
            return (ConvertRes)converter.convert(condition,beanInfo);
        }
    }
}
