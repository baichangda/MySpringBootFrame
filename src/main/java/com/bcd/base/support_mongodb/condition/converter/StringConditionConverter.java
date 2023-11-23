package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class StringConditionConverter implements Converter<StringCondition, Criteria> {
    @Override
    public Criteria convert(StringCondition condition, Object... exts) {
        String fieldName = condition.fieldName;
        Object val = condition.val;
        StringCondition.Handler handler = condition.handler;
        Criteria criteria = null;
        if (val != null) {
            criteria = Criteria.where(fieldName);
            switch (handler){
                case EQUAL: {
                    criteria.is(val);
                    break;
                }
                case NOT_EQUAL: {
                    criteria.ne(val);
                    break;
                }
                case ALL_LIKE: {
                    criteria.regex(".*(" + val + ").*");
                    break;
                }
                case LEFT_LIKE: {
                    criteria.regex("^(" + val + ")");
                    break;
                }
                case RIGHT_LIKE: {
                    criteria.regex("(" + val + ")$");
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
                        throw BaseRuntimeException.getException("type[{}] not support",val.getClass().getName());
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
                        throw BaseRuntimeException.getException("type[{}] not support",val.getClass().getName());
                    }
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("handler[{}] not support",handler);
                }
            }
        }
        return criteria;
    }

    public static void main(String[] args) {
        Criteria criteria = Criteria.where("id").in(Arrays.asList("1", "2"));
        Query query = Query.query(criteria);
        System.out.println(query);
    }

}
