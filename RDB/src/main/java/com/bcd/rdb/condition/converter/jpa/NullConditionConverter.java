package com.bcd.rdb.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.NullCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.rdb.util.ConditionUtil;

import javax.persistence.criteria.*;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class NullConditionConverter implements Converter<NullCondition,Predicate> {
    @Override
    public Predicate convert(NullCondition condition, Object... exts) {
        Predicate predicate;
        NullCondition.Handler handler= condition.handler;
        String fieldName=condition.fieldName;
        Root root=(Root)exts[0];
        CriteriaBuilder cb=(CriteriaBuilder)exts[2];
        Path path = ConditionUtil.parseRootPath(root,fieldName);
        switch (handler) {
            case NULL: {
                predicate=cb.isNull(path);
                break;
            }
            case NOT_NULL: {
                predicate=cb.isNotNull(path);
                break;
            }
            default :{
                throw BaseRuntimeException.getException("[NullConditionConverter.convert],Do Not Support ["+handler+"]!");
            }
        }
        return predicate;
    }
}
