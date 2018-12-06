package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;


/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class DateConditionConverter  implements Converter<DateCondition,Criteria> {
    @Override
    public Criteria convert(DateCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        Object val=condition.val;
        DateCondition.Handler handler=condition.handler;
        Criteria criteria= null;
        if(val!=null) {
            criteria=Criteria.where(fieldName);
            switch (handler) {
                case EQUAL: {
                    criteria.is(val);
                    break;
                }
                case LE: {
                    criteria.lte(val);
                    break;
                }
                case LT: {
                    criteria.lt(val);
                    break;
                }
                case GE: {
                    criteria.gte(val);
                    break;
                }
                case GT: {
                    criteria.gt(val);
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[DateConditionConverter.convert],Do Not Support ["+handler+"]!");
                }
            }
        }
        return criteria;
    }
}
