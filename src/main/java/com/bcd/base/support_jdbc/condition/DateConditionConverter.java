package com.bcd.base.support_jdbc.condition;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.MyException;
import com.bcd.base.support_jdbc.service.BeanInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/9/15.
 */
public class DateConditionConverter implements Converter<DateCondition, ConvertRes> {
    @Override
    public ConvertRes convert(DateCondition condition, Object... exts) {
        final Object val = condition.val;
        if (val == null) {
            return null;
        }
        final String fieldName = condition.fieldName;
        final DateCondition.Handler handler = condition.handler;
        final BeanInfo<?> beanInfo = (BeanInfo<?>) exts[0];
        final String columnName = beanInfo.toColumnName(fieldName);
        switch (handler) {
            case EQUAL: {
                return new ConvertRes(columnName + "=?", new ArrayList<>(List.of(val)));
            }
            case LE: {
                return new ConvertRes(columnName + "<=?", new ArrayList<>(List.of(val)));
            }
            case LT: {
                return new ConvertRes(columnName + "<?", new ArrayList<>(List.of(val)));
            }
            case GE: {
                return new ConvertRes(columnName + ">=?", new ArrayList<>(List.of(val)));
            }
            case GT: {
                return new ConvertRes(columnName + ">?", new ArrayList<>(List.of(val)));
            }
            case BETWEEN: {
                final Date[] dates = (Date[]) val;
                if (dates[0] != null && dates[1] != null) {
                    StringBuilder sql = new StringBuilder();
                    List<Object> paramList = new ArrayList<>(List.of(dates));
                    sql.append(columnName);
                    sql.append(">=? AND ");
                    sql.append(columnName);
                    sql.append("<?");
                    return new ConvertRes(sql.toString(), paramList);
                } else if (dates[0] != null) {
                    StringBuilder sql = new StringBuilder();
                    List<Object> paramList = new ArrayList<>();
                    paramList.add(dates[0]);
                    sql.append(columnName);
                    sql.append(">=?");
                    return new ConvertRes(sql.toString(), paramList);
                } else if (dates[1] != null) {
                    StringBuilder sql = new StringBuilder();
                    List<Object> paramList = new ArrayList<>();
                    paramList.add(dates[1]);
                    sql.append(columnName);
                    sql.append("<?");
                    return new ConvertRes(sql.toString(), paramList);
                } else {
                    return null;
                }
            }
            default: {
                throw MyException.get("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
            }
        }

    }
}
