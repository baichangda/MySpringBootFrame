package com.base.condition.impl;

import com.base.condition.BaseCondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/23.
 */
@SuppressWarnings("unchecked")
public class BaseConditionImpl extends BaseCondition{
    public ConcatWay concatWay;
    public List<BaseCondition> childrenList=new ArrayList<>();
    public enum ConcatWay{
        AND,
        OR
    }
    @Override
    public <T> Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicateList=new ArrayList<>();
        childrenList.forEach(children->{
            Predicate predicate=children.toPredicate(root,query,cb);
            if(predicate!=null){
                predicateList.add(predicate);
            }
        });
        if(predicateList==null||predicateList.size()==0){
            return null;
        }
        if(ConcatWay.AND.equals(concatWay)){
            return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
        }else if(ConcatWay.OR.equals(concatWay)){
            return cb.or(predicateList.toArray(new Predicate[predicateList.size()]));
        }else{
            return null;
        }
    }

    public BaseConditionImpl(){

    }
    public BaseConditionImpl(ConcatWay concatWay,List<BaseCondition> childrenList){
        this.concatWay=concatWay;
        this.childrenList=childrenList;
    }

}
