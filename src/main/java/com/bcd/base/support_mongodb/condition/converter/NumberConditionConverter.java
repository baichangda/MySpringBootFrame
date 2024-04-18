package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.MyException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter implements Converter<NumberCondition, Criteria> {
    @Override
    public Criteria convert(NumberCondition condition, Object... exts) {
        String fieldName = condition.fieldName;
        Object val = condition.val;
        NumberCondition.Handler handler = condition.handler;
        Criteria criteria = null;
        if (val != null) {
            criteria = Criteria.where(fieldName);
            switch (handler) {
                case EQUAL: {
                    criteria.is(val);
                    break;
                }
                case LT: {
                    criteria.lt(val);
                    break;
                }
                case LE: {
                    criteria.lte(val);
                    break;
                }
                case GT: {
                    criteria.gt(val);
                    break;
                }
                case GE: {
                    criteria.gte(val);
                    break;
                }
                case NOT_EQUAL: {
                    criteria.ne(val);
                    break;
                }
                case IN: {
                    if (val.getClass().isArray()) {
                        List<Object> notEmptyList =new ArrayList<>();
                        int length = Array.getLength(val);
                        for (int i = 0; i < length; i++) {
                            notEmptyList.add(Array.get(val, i));
                        }
                        criteria.in(notEmptyList);
                    } else {
                        throw MyException.get("type[{}] not support",val.getClass().getName());
                    }
                    break;
                }
                case NOT_IN: {
                    if (val.getClass().isArray()) {
                        List<Object> notEmptyList =new ArrayList<>();
                        int length = Array.getLength(val);
                        for (int i = 0; i < length; i++) {
                            notEmptyList.add(Array.get(val, i));
                        }
                        criteria.nin(notEmptyList);
                    } else {
                        throw MyException.get("type[{}] not support",val.getClass().getName());
                    }
                    break;
                }
                default: {
                    throw MyException.get("handler[{}] not support",handler);
                }
            }
        }
        return criteria;
    }
}
