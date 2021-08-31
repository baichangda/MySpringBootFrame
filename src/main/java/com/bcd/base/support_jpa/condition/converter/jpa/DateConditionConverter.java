package com.bcd.base.support_jpa.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jpa.util.ConditionUtil;

import javax.persistence.criteria.*;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class DateConditionConverter implements Converter<DateCondition, Predicate> {
    @Override
    public Predicate convert(DateCondition condition, Object... exts) {
        Predicate predicate = null;
        DateCondition.Handler handler = condition.handler;
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
                case LE: {
                    predicate = cb.lessThanOrEqualTo(path, (Date) val);
                    break;
                }
                case LT: {
                    predicate = cb.lessThan(path, (Date) val);
                    break;
                }
                case GE: {
                    predicate = cb.greaterThanOrEqualTo(path, (Date) val);
                    break;
                }
                case GT: {
                    predicate = cb.greaterThan(path, (Date) val);
                    break;
                }
                default: {
                    throw BaseRuntimeException.getException("[DateConditionConverter.convert],Do Not Support [" + handler + "]!");
                }
            }
        }
        return predicate;
    }
}
