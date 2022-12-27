package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConcatCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/9/15.
 */
public class ConcatConditionConverter implements Converter<ConcatCondition, ConvertRes> {
    @Override
    public ConvertRes convert(ConcatCondition condition, Object... exts) {
        ConvertRes[] arr = condition.conditions.stream().map(e -> ConditionUtil.convertCondition(e)).filter(Objects::nonNull).toArray(ConvertRes[]::new);
        ConcatCondition.ConcatWay concatWay = condition.concatWay;
        if (arr.length == 0) {
            return null;
        } else if (arr.length == 1) {
            return arr[0];
        } else {
            StringBuilder sql = new StringBuilder();
            List paramList = new ArrayList();
            for (int i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sql.append(arr[i].sql);
                } else {
                    sql.append(" ");
                    sql.append(concatWay.toString());
                    sql.append(" ");
                    sql.append(arr[i].sql);
                }
                paramList.addAll(arr[i].paramList);
            }
            return new ConvertRes(sql.toString(), paramList);
        }
    }
}
