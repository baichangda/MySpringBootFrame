package com.bcd.base.condition;


import com.bcd.base.condition.impl.ConcatCondition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/11.
 */
public interface Condition extends Serializable {
    static Condition and(List<Condition> conditionList) {
        return new ConcatCondition(ConcatCondition.ConcatWay.AND, conditionList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    static Condition and(Condition... conditionArr) {
        return and(Arrays.asList(conditionArr));
    }

    static Condition or(List<Condition> conditionList) {
        return new ConcatCondition(ConcatCondition.ConcatWay.OR, conditionList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    static Condition or(Condition... conditionArr) {
        return or(Arrays.asList(conditionArr));
    }

    String toAnalysis();
}
