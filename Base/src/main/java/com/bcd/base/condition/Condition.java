package com.bcd.base.condition;


import com.bcd.base.condition.impl.ConditionImpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/11.
 */
@SuppressWarnings("unchecked")
public abstract class Condition implements Serializable{
    public String fieldName;
    public Object val;

    public static Condition and(List<Condition> conditionList){
        return new ConditionImpl(ConditionImpl.ConcatWay.AND,conditionList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public static Condition and(Condition... conditionArr){
        return and(Arrays.asList(conditionArr));
    }

    public static Condition or(List<Condition> conditionList){
        return new ConditionImpl(ConditionImpl.ConcatWay.OR, conditionList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public static Condition or(Condition... conditionArr){
        return or(Arrays.asList(conditionArr));
    }
}
