package com.base.condition.impl;

import com.base.condition.BaseCondition;

import javax.persistence.criteria.*;

/**
 * Created by Administrator on 2017/6/8.
 */
public class NullCondition extends BaseCondition{
    private Handler handler;
    public enum Handler{
        /**
         * 为空
         */
        NULL,
        /**
         * 不为空
         */
        NOT_NULL
    }


    public NullCondition(String fieldName, Handler handler){
        super(fieldName);
        this.handler=handler;
    }

    public NullCondition(String fieldName){
        super(fieldName);
        this.handler= Handler.NULL;

    }

    public <T> Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate=null;
        Path path = parseRootPath(root,fieldName);
        switch (handler) {
            case NULL: {
                predicate=cb.isNull(path);
                break;
            }
            case NOT_NULL: {
                predicate=cb.isNotNull(path);
                break;
            }
        }
        return predicate;
    }
}
