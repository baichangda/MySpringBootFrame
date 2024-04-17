package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Created by Administrator on 2017/9/15.
 */
public class NullConditionConverter implements Converter<NullCondition, Criteria> {
    @Override
    public Criteria convert(NullCondition condition, Object... exts) {
        String fieldName = condition.fieldName;
        NullCondition.Handler handler = condition.handler;
        Criteria criteria = Criteria.where(fieldName);
        switch (handler) {
            case NULL: {
                criteria.orOperator(criteria.exists(false), criteria.exists(true).is(null));
                break;
            }
            case NOT_NULL: {
                criteria.exists(true).ne(null);
                break;
            }
            default: {
                throw BaseRuntimeException.get("[NullConditionConverter.convert],Do Not Support [" + handler + "]!");
            }
        }
        return criteria;
    }
}
