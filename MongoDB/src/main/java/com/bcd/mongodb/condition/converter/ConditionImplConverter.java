package com.bcd.mongodb.condition.converter;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConditionImpl;
import com.bcd.mongodb.util.ConditionUtil;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Objects;


/**
 * Created by Administrator on 2017/9/15.
 */
@SuppressWarnings("unchecked")
public class ConditionImplConverter implements Converter<ConditionImpl,Criteria> {
    @Override
    public Criteria convert(ConditionImpl condition, Object... exts) {
        List<Condition> conditionList= condition.childrenList;
        ConditionImpl.ConcatWay concatWay=condition.concatWay;
        Criteria[] criterias= conditionList.stream().map(ConditionUtil::convertCondition).filter(Objects::nonNull).toArray(Criteria[]::new);
        if(criterias.length == 0){
            return null;
        }
        switch (concatWay){
            case AND:{
                return new Criteria().andOperator(criterias);
            }
            case OR:{
                return new Criteria().orOperator(criterias);
            }
            default:{
                return null;
            }
        }
    }
}
