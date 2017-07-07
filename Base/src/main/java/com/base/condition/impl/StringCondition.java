package com.base.condition.impl;


import com.base.condition.BaseCondition;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;

/**
 * Created by Administrator on 2016/12/30.
 */
@SuppressWarnings("unchecked")
public class StringCondition extends BaseCondition{
    private Handler handler;

    public enum Handler{
        /**
         * 等于
         */
        EQUAL,
        /**
         * 不等于
         */
        NOT_EQUAL,
        /**
         * 全匹配
         */
        ALL_LIKE,
        /**
         * 左匹配
         */
        LEFT_LIKE,
        /**
         * 右匹配
         */
        RIGHT_LIKE
    }

    public StringCondition(String fieldName,Object val,Handler handler){
        super(fieldName,val);
        this.handler=handler;
    }

    public StringCondition(String fieldName,Object val){
        super(fieldName,val);
        this.handler= Handler.EQUAL;
    }

    public <T>Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb){
        Predicate predicate=null;
        if(!StringUtils.isEmpty(val)){
            Path path= parseRootPath(root,fieldName);
            switch (handler){
                case EQUAL: {
                    predicate=cb.equal(path, val);
                    break;
                }
                case NOT_EQUAL: {
                    predicate=cb.notEqual(path, val);
                    break;
                }
                case ALL_LIKE: {
                    predicate=cb.like(path, "%" + val.toString() + "%");
                    break;
                }
                case LEFT_LIKE: {
                    predicate=cb.like(path, "%" + val.toString());
                    break;
                }
                case RIGHT_LIKE: {
                    predicate=cb.like(path, val.toString() + "%");
                    break;
                }
            }
        }
        return predicate;
    }
}
