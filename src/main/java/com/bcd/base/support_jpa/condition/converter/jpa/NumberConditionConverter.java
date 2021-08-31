package com.bcd.base.support_jpa.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.util.ConditionUtil;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NumberConditionConverter implements Converter<NumberCondition, Predicate> {
    @Override
    public Predicate convert(NumberCondition condition, Object... exts) {
        Predicate predicate = null;
        NumberCondition.Handler handler = condition.handler;
        Object val = condition.val;
        String fieldName = condition.fieldName;
        Root root = (Root) exts[0];
        CriteriaQuery query = (CriteriaQuery) exts[1];
        CriteriaBuilder cb = (CriteriaBuilder) exts[2];
        if (val != null) {
            Path path = ConditionUtil.parseRootPath(root, fieldName);
            switch (handler) {
                case EQUAL: {
                    predicate = cb.equal(path, val);
                    break;
                }
                case LT: {
                    predicate = cb.lt(path, (Number) val);
                    break;
                }
                case LE: {
                    predicate = cb.le(path, (Number) val);
                    break;
                }
                case GT: {
                    predicate = cb.gt(path, (Number) val);
                    break;
                }
                case GE: {
                    predicate = cb.ge(path, (Number) val);
                    break;
                }
                case NOT_EQUAL: {
                    predicate = cb.notEqual(path, val);
                    break;
                }
                case IN: {
                    if (val instanceof Collection) {
                        List notEmptyList = (List) ((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        predicate = path.in(notEmptyList);
                    } else {
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                case NOT_IN: {
                    if (val instanceof Collection) {
                        List notEmptyList = (List) ((Collection) val).stream().filter(Objects::nonNull).collect(Collectors.toList());
                        predicate = cb.not(path.in(notEmptyList));
                    } else {
                        throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Value Must be Collection Instance!");
                    }
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[NumberConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
        return predicate;
    }
}
