package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.BooleanCondition;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Created by Administrator on 2017/10/11.
 */
public class BooleanConditionConverter implements Converter<BooleanCondition,Criteria>{
    @Override
    public Criteria convert(BooleanCondition condition, Object... exts) {
        String fieldName=condition.fieldName;
        Object val=condition.val;
        Criteria criteria= null;
        if(val!=null){
            criteria=Criteria.where(fieldName).is(val);
        }
        return criteria;
    }
}
