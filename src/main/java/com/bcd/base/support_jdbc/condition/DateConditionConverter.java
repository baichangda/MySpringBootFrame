package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class DateConditionConverter implements Converter<DateCondition, ConvertRes> {
    @Override
    public ConvertRes convert(DateCondition condition, Object... exts) {
        final Object val = condition.val;
        final String fieldName = condition.fieldName;
        final DateCondition.Handler handler = condition.handler;
        StringBuilder sql = new StringBuilder();
        List paramList = new ArrayList<>();
        paramList.add(val);
        if (val != null) {
            switch (handler) {
                case EQUAL: {
                    sql.append(fieldName);
                    sql.append("=");
                    sql.append("?");
                    break;
                }
                case LE: {
                    sql.append(fieldName);
                    sql.append("<=");
                    sql.append("?");
                    break;
                }
                case LT: {
                    sql.append(fieldName);
                    sql.append("<");
                    sql.append("?");
                    break;
                }
                case GE: {
                    sql.append(fieldName);
                    sql.append(">=");
                    sql.append("?");
                    break;
                }
                case GT: {
                    sql.append(fieldName);
                    sql.append(">");
                    sql.append("?");
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
        return new ConvertRes(sql.toString(), paramList);
    }
}
