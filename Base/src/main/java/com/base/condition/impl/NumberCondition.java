package com.base.condition.impl;


import com.base.condition.BaseCondition;

import javax.persistence.criteria.*;

/**
 * Created by Administrator on 2016/12/30.
 */
@SuppressWarnings("unchecked")
public class NumberCondition extends BaseCondition {
    private Handler handler;
    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 小于
         */
        LT,
        /**
         * 小于等于
         */
        LE,
        /**
         * 大于
         */
        GT,
        /**
         * 大于等于
         */
        GE,
        /**
         * 不等于
         */
        NOT_EQUAL
    }


    public NumberCondition(String fieldName, Object val, Handler handler){
        super(fieldName,val);
        this.handler=handler;
    }

    public NumberCondition(String fieldName, Object val){
        super(fieldName,val);
            this.handler= Handler.EQUAL;

    }

    public <T> Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate=null;
        if(val!=null) {
            Path path = parseRootPath(root,fieldName);
            switch (handler) {
                case EQUAL: {
                    predicate=cb.equal(path, val);
                    break;
                }
                case LT: {
                    predicate=cb.lt(path, (Number) val);
                    break;
                }
                case LE: {
                    predicate=cb.le(path, (Number) val);
                    break;
                }
                case GT: {
                    predicate=cb.gt(path, (Number) val);
                    break;
                }
                case GE: {
                    predicate=cb.ge(path, (Number) val);
                    break;
                }
                case NOT_EQUAL: {
                    predicate=cb.notEqual(path,  val);
                    break;
                }
            }
        }
        return predicate;
    }
}
