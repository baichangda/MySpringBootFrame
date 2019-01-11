package com.bcd.rdb.condition.converter.jpa;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConditionImpl;
import com.bcd.rdb.util.ConditionUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class ConditionImplConverter implements Converter<ConditionImpl,Predicate> {
    @Override
    public Predicate convert(ConditionImpl condition, Object... exts) {
        List<Condition> childrenList=condition.childrenList;
        ConditionImpl.ConcatWay concatWay=condition.concatWay;
        Root root=(Root)exts[0];
        CriteriaQuery query=(CriteriaQuery)exts[1];
        CriteriaBuilder cb=(CriteriaBuilder)exts[2];
        Predicate[] predicates= childrenList.stream().map(c->ConditionUtil.convertCondition(c,root,query,cb)).filter(Objects::nonNull).toArray(len->new Predicate[len]);
        if(ConditionImpl.ConcatWay.AND.equals(concatWay)){
            return cb.and(predicates);
        }else if(ConditionImpl.ConcatWay.OR.equals(concatWay)){
            return cb.or(predicates);
        }else{
            return null;
        }
    }
}
