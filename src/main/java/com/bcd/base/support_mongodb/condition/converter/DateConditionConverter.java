package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
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
        Criteria criteria = null;
        if (val != null) {
            switch (handler) {
                case EQUAL: {
                    criteria = Criteria.where(fieldName);
                    criteria.is(val);
                    break;
                }
                case LE: {
                    criteria = Criteria.where(fieldName);
                    criteria.lte(val);
                    break;
                }
                case LT: {
                    criteria = Criteria.where(fieldName);
                    criteria.lt(val);
                    break;
                }
                case GE: {
                    criteria = Criteria.where(fieldName);
                    criteria.gte(val);
                    break;
                }
                case GT: {
                    criteria = Criteria.where(fieldName);
                    criteria.gt(val);
                    break;
                }
                case BETWEEN: {
                    final Date[] dates = (Date[]) val;
                    if (dates[0] == null && dates[1] == null) {
                        return null;
                    }
                    criteria = Criteria.where(fieldName);
                    if (dates[0] != null) {
                        criteria.gte(dates[0]);
                    }
                    if (dates[1] != null) {
                        criteria.lt(dates[1]);
                    }
                    break;
                }
                default: {
                    throw BaseRuntimeException.get("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
        return criteria;
    }
}
