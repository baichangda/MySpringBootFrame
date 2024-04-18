package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.lang.reflect.Array;
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
        if (val == null) {
            return null;
        }
        final String fieldName = condition.fieldName;
        final StringCondition.Handler handler = condition.handler;
        final BeanInfo beanInfo = (BeanInfo) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        StringBuilder sql = new StringBuilder();
        List<Object> paramList = new ArrayList<>();
        switch (handler) {
            case EQUAL: {
                sql.append(columnName);
                sql.append("=?");
                paramList.add(val);
                break;
            }
            case NOT_EQUAL: {
                sql.append(columnName);
                sql.append("<>?");
                paramList.add(val);
                break;
            }
            case ALL_LIKE: {
                sql.append(columnName);
                sql.append(" like ?");
                paramList.add("%" + val + "%");
                break;
            }
            case LEFT_LIKE: {
                sql.append(columnName);
                sql.append(" like ?");
                paramList.add("%" + val);
                break;
            }
            case RIGHT_LIKE: {
                sql.append(columnName);
                sql.append(" like ?");
                paramList.add(val + "%");
                break;
            }
            case IN: {
                if (val.getClass().isArray()) {
                    int length = Array.getLength(val);
                    sql.append(columnName);
                    sql.append(" in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < length; i++) {
                        sj.add("?");
                        paramList.add(Array.get(val, i));
                    }
                    sql.append(sj);
                    sql.append(")");
                } else {
                    throw MyException.get("type[{}] not support",val.getClass().getName());
                }
            }
            case NOT_IN: {
                if (val.getClass().isArray()) {
                    List<Object> notEmptyList = ((Collection<Object>) val).stream().filter(e -> e != null && !e.toString().isEmpty()).collect(Collectors.toList());
                    sql.append(columnName);
                    sql.append(" not in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < notEmptyList.size(); i++) {
                        sj.add("?");
                    }
                    sql.append(sj);
                    sql.append(")");
                    paramList.addAll(notEmptyList);
                } else {
                    throw MyException.get("type[{}] not support",val.getClass().getName());
                }
                break;
            }
            default: {
                throw MyException.get("handler[{}] not support",handler);
            }
        }
        return new ConvertRes(sql.toString(), paramList);
    }

}
