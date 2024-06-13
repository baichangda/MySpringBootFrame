package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Administrator on 2017/9/15.
 */
public class NumberConditionConverter implements Converter<NumberCondition, ConvertRes> {
    @Override
    public ConvertRes convert(NumberCondition condition, Object... exts) {
        final Object val = condition.val;
        if (val == null) {
            return null;
        }
        final String fieldName = condition.fieldName;
        final NumberCondition.Handler handler = condition.handler;
        final BeanInfo<?> beanInfo = (BeanInfo<?>) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        switch (handler) {
            case EQUAL: {
                return new ConvertRes(columnName + "=?", new ArrayList<>(List.of(val)));
            }
            case LT: {
                return new ConvertRes(columnName + "<?", new ArrayList<>(List.of(val)));
            }
            case LE: {
                return new ConvertRes(columnName + "<=?", new ArrayList<>(List.of(val)));
            }
            case GT: {
                return new ConvertRes(columnName + ">?", new ArrayList<>(List.of(val)));
            }
            case GE: {
                return new ConvertRes(columnName + ">=?", new ArrayList<>(List.of(val)));
            }
            case NOT_EQUAL: {
                return new ConvertRes(columnName + "<>?", new ArrayList<>(List.of(val)));
            }
            case IN: {
                if (val.getClass().isArray()) {
                    StringBuilder sql = new StringBuilder();
                    List<Object> paramList = new ArrayList<>();
                    int length = Array.getLength(val);
                    sql.append(columnName);
                    sql.append(" in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(val, i);
                        if (o != null) {
                            sj.add("?");
                            paramList.add(o);
                        }
                    }
                    sql.append(sj);
                    sql.append(")");
                    if (paramList.isEmpty()) {
                        return null;
                    } else {
                        return new ConvertRes(sql.toString(), paramList);
                    }
                } else {
                    throw BaseException.get("type[{}] not support", val.getClass().getName());
                }
            }
            case NOT_IN: {
                if (val.getClass().isArray()) {
                    StringBuilder sql = new StringBuilder();
                    List<Object> paramList = new ArrayList<>();
                    int length = Array.getLength(val);
                    sql.append(columnName);
                    sql.append(" not in (");
                    StringJoiner sj = new StringJoiner(",");
                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(val, i);
                        if (o != null) {
                            sj.add("?");
                            paramList.add(o);
                        }
                    }
                    sql.append(sj);
                    sql.append(")");
                    if (paramList.isEmpty()) {
                        return null;
                    } else {
                        return new ConvertRes(sql.toString(), paramList);
                    }
                } else {
                    throw BaseException.get("type[{}] not support", val.getClass().getName());
                }
            }
            default: {
                throw BaseException.get("handler[{}] not support", handler);
            }
        }

    }
}
