package com.bcd.base.support_jdbc.condition;


import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.*;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.util.HashMap;
import java.util.Map;

public class ConditionUtil {

    private final static Map<Class<? extends Condition>, Converter<? extends Condition,?>> CONDITION_CONVERTER_MAP = new HashMap<>();

    static {
        CONDITION_CONVERTER_MAP.put(NumberCondition.class, new NumberConditionConverter());
        CONDITION_CONVERTER_MAP.put(StringCondition.class, new StringConditionConverter());
        CONDITION_CONVERTER_MAP.put(DateCondition.class, new DateConditionConverter());
        CONDITION_CONVERTER_MAP.put(NullCondition.class, new NullConditionConverter());
        CONDITION_CONVERTER_MAP.put(ConcatCondition.class, new ConcatConditionConverter());
    }

    public static  <T extends Condition>ConvertRes convertCondition(T condition, BeanInfo<?> beanInfo) {
        return convertCondition(condition, beanInfo, true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Condition> ConvertRes convertCondition(T condition, BeanInfo<?> beanInfo, boolean root) {
        if (condition == null) {
            return null;
        }
        Converter<T,?> converter = (Converter<T, ?>) CONDITION_CONVERTER_MAP.get(condition.getClass());
        if (converter == null) {
            throw BaseRuntimeException.get("[ConditionUtil.convertCondition],Condition[" + condition.getClass() + "] Have Not Converter!");
        } else {
            return (ConvertRes) converter.convert(condition, beanInfo, root);
        }
    }
}
