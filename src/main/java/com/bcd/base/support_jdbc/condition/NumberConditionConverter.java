package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseRuntimeException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter implements Converter<NumberCondition, ConvertRes> {
    @Override
    public ConvertRes convert(NumberCondition condition, Object... exts) {
        final Object val = condition.val;
        final String fieldName = condition.fieldName;
        final NumberCondition.Handler handler = condition.handler;
        final BeanInfo beanInfo = (BeanInfo) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        StringBuilder sql = new StringBuilder();
        List<Object> paramList = new ArrayList<>();
        if (val != null) {
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
                    if (val instanceof Collection) {
                        List notEmptyList = ((Collection<Object>) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        sql.append(columnName);
                        sql.append("in (");
                        StringJoiner sj = new StringJoiner(",");
                        for (int i = 0; i < notEmptyList.size(); i++) {
                            sj.add("?");
                        }
                        sql.append(sj);
                        sql.append(")");
                        paramList.addAll(notEmptyList);
                    } else {
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if (val instanceof Collection) {
                        List<Object> notEmptyList = ((Collection<Object>) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        sql.append(columnName);
                        sql.append("not in (");
                        StringJoiner sj = new StringJoiner(",");
                        for (int i = 0; i < notEmptyList.size(); i++) {
                            sj.add("?");
                        }
                        sql.append(sj);
                        sql.append(")");
                        paramList.addAll(notEmptyList);
                    } else {
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
            return new ConvertRes(sql.toString(), paramList);
        } else {
            return null;
        }

    }
}
