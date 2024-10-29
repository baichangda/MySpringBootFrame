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
    static Condition and(List<Condition> conditions) {
        return new ConcatCondition(ConcatCondition.ConcatWay.AND, conditions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    static Condition and(Condition... conditions) {
        return and(Arrays.asList(conditions));
    }

    static Condition or(List<Condition> conditions) {
        return new ConcatCondition(ConcatCondition.ConcatWay.OR, conditions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    static Condition or(Condition... conditions) {
        return or(Arrays.asList(conditions));
    }
}
