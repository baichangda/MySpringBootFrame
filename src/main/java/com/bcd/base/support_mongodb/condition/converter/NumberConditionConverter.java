package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/15.
 */
public class NumberConditionConverter implements Converter<NumberCondition, Criteria> {
    @Override
    public Criteria convert(NumberCondition condition, Object... exts) {
        String fieldName = condition.fieldName;
        Object val = condition.val;
        NumberCondition.Handler handler = condition.handler;
        if (val == null) {
            return null;
        } else {
            switch (handler) {
                case EQUAL: {
                    return Criteria.where(fieldName).is(val);
                }
                case LT: {
                    return Criteria.where(fieldName).lt(val);
                }
                case LE: {
                    return Criteria.where(fieldName).lte(val);
                }
                case GT: {
                    return Criteria.where(fieldName).gt(val);
                }
                case GE: {
                    return Criteria.where(fieldName).gte(val);
                }
                case NOT_EQUAL: {
                    return Criteria.where(fieldName).ne(val);
                }
                case IN: {
                    if (val.getClass().isArray()) {
                        List<Object> list = new ArrayList<>();
                        int length = Array.getLength(val);
                        for (int i = 0; i < length; i++) {
                            Object o = Array.get(val, i);
                            if (o != null) {
                                list.add(o);
                            }
                        }
                        if (list.isEmpty()) {
                            return null;
                        } else {
                            return Criteria.where(fieldName).in(list);
                        }
                    } else {
                        throw BaseException.get("type[{}] not support", val.getClass().getName());
                    }
                }
                case NOT_IN: {
                    if (val.getClass().isArray()) {
                        List<Object> list = new ArrayList<>();
                        int length = Array.getLength(val);
                        for (int i = 0; i < length; i++) {
                            Object o = Array.get(val, i);
                            if (o != null) {
                                list.add(o);
                            }
                        }
                        if (list.isEmpty()) {
                            return null;
                        } else {
                            return Criteria.where(fieldName).nin(list);
                        }
                    } else {
                        throw BaseException.get("type[{}] not support", val.getClass().getName());
                    }
                }
                default: {
                    throw BaseException.get("handler[{}] not support", handler);
                }
            }
        }
    }
}
