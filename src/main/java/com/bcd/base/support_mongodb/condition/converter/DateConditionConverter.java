package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Date;


/**
 * Created by Administrator on 2017/9/15.
 */
public class DateConditionConverter implements Converter<DateCondition, Criteria> {
    @Override
    public Criteria convert(DateCondition condition, Object... exts) {
        String fieldName = condition.fieldName;
        Object val = condition.val;
        DateCondition.Handler handler = condition.handler;
        if (val == null) {
            return null;
        } else {
            switch (handler) {
                case EQUAL: {
                    return Criteria.where(fieldName).is(val);
                }
                case LE: {
                    return Criteria.where(fieldName).lte(val);
                }
                case LT: {
                    return Criteria.where(fieldName).lt(val);
                }
                case GE: {
                    return Criteria.where(fieldName).gte(val);
                }
                case GT: {
                    return Criteria.where(fieldName).gt(val);
                }
                case BETWEEN: {
                    final Date[] dates = (Date[]) val;
                    if (dates[0] == null && dates[1] == null) {
                        return null;
                    }
                    Criteria criteria = Criteria.where(fieldName);
                    if (dates[0] != null) {
                        criteria.gte(dates[0]);
                    }
                    if (dates[1] != null) {
                        criteria.lt(dates[1]);
                    }
                    return criteria;
                }
                default: {
                    throw BaseException.get("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
    }
}
