package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConcatCondition;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/9/15.
 */
public class ConcatConditionConverter implements Converter<ConcatCondition, ConvertRes> {
    @Override
    public ConvertRes convert(ConcatCondition condition, Object... exts) {
        final BeanInfo beanInfo = (BeanInfo) exts[0];
        final boolean root = exts.length != 1;
        ConvertRes[] arr = condition.conditions.stream().map(e -> ConditionUtil.convertCondition(e, beanInfo, false)).filter(Objects::nonNull).toArray(ConvertRes[]::new);
        if (arr.length == 0) {
            return null;
        } else if (arr.length == 1) {
            return arr[0];
        } else {
            ConcatCondition.ConcatWay concatWay = condition.concatWay;
            StringBuilder sql;
            if (root) {
                sql = new StringBuilder();
            } else {
                sql = new StringBuilder("(");
            }
            List<Object> paramList = new ArrayList<>();
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
            if (!root) {
                sql.append(")");
            }
            return new ConvertRes(sql.toString(), paramList);
        }
    }
}
