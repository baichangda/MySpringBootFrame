package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.service.BeanInfo;

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
        if (val == null) {
            return null;
        }
        final String fieldName = condition.fieldName;
        final DateCondition.Handler handler = condition.handler;
        final BeanInfo beanInfo = (BeanInfo) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        StringBuilder sql = new StringBuilder();
        List<Object> paramList = new ArrayList<>();
        paramList.add(val);
        switch (handler) {
            case EQUAL: {
                sql.append(columnName);
                sql.append("=?");
                break;
            }
            case LE: {
                sql.append(columnName);
                sql.append("<=?");
                break;
            }
            case LT: {
                sql.append(columnName);
                sql.append("<?");
                break;
            }
            case GE: {
                sql.append(columnName);
                sql.append(">=?");
                break;
            }
            case GT: {
                sql.append(columnName);
                sql.append(">?");
                break;
            }
            default: {
                throw BaseRuntimeException.getException("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
            }
        }
        return new ConvertRes(sql.toString(), paramList);
    }
}
