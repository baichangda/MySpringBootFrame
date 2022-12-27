package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class StringConditionConverter implements Converter<StringCondition, ConvertRes> {
    @Override
    public ConvertRes convert(StringCondition condition, Object... exts) {
        final Object val = condition.val;
        final String fieldName = condition.fieldName;
        final StringCondition.Handler handler = condition.handler;
        StringBuilder sql = new StringBuilder();
        List paramList = new ArrayList<>();

        if (val != null) {
            switch (handler) {
                case EQUAL: {
                    sql.append(fieldName);
                    sql.append("=?");
                    paramList.add(val);
                    break;
                }
                case NOT_EQUAL: {
                    sql.append(fieldName);
                    sql.append("<>?");
                    paramList.add(val);
                    break;
                }
                case ALL_LIKE: {
                    sql.append(fieldName);
                    sql.append("like ?");
                    paramList.add("%" + val + "%");
                    break;
                }
                case LEFT_LIKE: {
                    sql.append(fieldName);
                    sql.append("like ?");
                    paramList.add("%" + val);
                    break;
                }
                case RIGHT_LIKE: {
                    sql.append(fieldName);
                    sql.append("like ?");
                    paramList.add(val + "%");
                    break;
                }
                case IN: {
                    List notEmptyList = (List) ((Collection) val).stream().filter(e -> e != null && !e.toString().isEmpty()).collect(Collectors.toList());
                    sql.append(fieldName);
                    sql.append("in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < notEmptyList.size(); i++) {
                        sj.add("?");
                    }
                    sql.append(sj);
                    sql.append(")");
                    paramList.addAll(notEmptyList);
                    break;
                }
                case NOT_IN: {
                    List notEmptyList = (List) ((Collection) val).stream().filter(e -> e != null && !e.toString().isEmpty()).collect(Collectors.toList());
                    sql.append(fieldName);
                    sql.append("not in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < notEmptyList.size(); i++) {
                        sj.add("?");
                    }
                    sql.append(sj);
                    sql.append(")");
                    paramList.addAll(notEmptyList);
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[StringConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
        return new ConvertRes(sql.toString(),paramList);
    }

}
