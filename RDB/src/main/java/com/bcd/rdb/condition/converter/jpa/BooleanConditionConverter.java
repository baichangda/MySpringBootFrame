package com.bcd.rdb.condition.converter.jpa;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.BooleanCondition;
import com.bcd.rdb.util.ConditionUtil;

import javax.persistence.criteria.*;

/**
 * Created by Administrator on 2017/10/11.
 */
@SuppressWarnings("unchecked")
public class BooleanConditionConverter implements Converter<BooleanCondition,Predicate>{
    @Override
    public Predicate convert(BooleanCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        Object val=condition.val;
        Root root=(Root)exts[0];
        CriteriaQuery query=(CriteriaQuery)exts[1];
        CriteriaBuilder cb=(CriteriaBuilder)exts[2];
        Predicate predicate= null;
        if(val!=null){
            Path path = ConditionUtil.parseRootPath(root, fieldName);
            predicate = cb.equal(path, val);
        }
        return predicate;
    }
}
