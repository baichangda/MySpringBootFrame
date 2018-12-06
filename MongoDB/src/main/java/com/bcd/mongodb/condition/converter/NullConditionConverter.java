package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NullConditionConverter implements Converter<NullCondition,Criteria> {
    @Override
    public Criteria convert(NullCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        NullCondition.Handler handler=condition.handler;
        Criteria criteria= Criteria.where(fieldName);
        switch (handler) {
            case NULL: {
                criteria.exists(false);
                break;
            }
            case NOT_NULL: {
                criteria.exists(true).ne(null);
                break;
            }
            default :{
                throw BaseRuntimeException.getException("[NullConditionConverter.convert],Do Not Support ["+handler+"]!");
            }
        }
        return criteria;
    }
}
