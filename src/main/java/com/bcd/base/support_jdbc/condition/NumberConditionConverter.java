package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter implements Converter<NumberCondition, ConvertRes> {
    @Override
    public ConvertRes convert(NumberCondition condition, Object... exts) {
        final Object val = condition.val;
        if (val == null) {
            return null;
        }
        final String fieldName = condition.fieldName;
        final NumberCondition.Handler handler = condition.handler;
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
            case LT: {
                sql.append(columnName);
                sql.append("<?");
                paramList.add(val);
                break;
            }
            case LE: {
                sql.append(columnName);
                sql.append("<=?");
                paramList.add(val);
                break;
            }
            case GT: {
                sql.append(columnName);
                sql.append(">?");
                paramList.add(val);
                break;
            }
            case GE: {
                sql.append(columnName);
                sql.append(">=?");
                paramList.add(val);
                break;
            }
            case NOT_EQUAL: {
                sql.append(columnName);
                sql.append("<>?");
                paramList.add(val);
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
                    throw BaseRuntimeException.getException("type[{}] not support",val.getClass().getName());
                }
                break;
            }
            case NOT_IN: {
                if (val instanceof Collection) {
                    List<Object> notEmptyList = ((Collection<Object>) val).stream().filter(Objects::nonNull).toList();
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
                    throw BaseRuntimeException.getException("type[{}] not support",val.getClass().getName());
                }
                break;
            }
            default: {
                throw BaseRuntimeException.getException("handler[{}] not support",handler);
            }
        }
        return new ConvertRes(sql.toString(), paramList);

    }
}
