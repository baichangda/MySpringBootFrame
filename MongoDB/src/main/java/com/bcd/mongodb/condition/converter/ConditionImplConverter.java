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
        Criteria[] criterias= conditionList.stream().map(c-> ConditionUtil.convertCondition(c)).filter(Objects::nonNull).toArray(len->new Criteria[len]);
        if(criterias==null||criterias.length==0){
            return null;
        }
        if(ConditionImpl.ConcatWay.AND.equals(concatWay)){
            return new Criteria().andOperator(criterias);
        }else if(ConditionImpl.ConcatWay.OR.equals(concatWay)){
            return new Criteria().orOperator(criterias);
        }else{
            return null;
        }
    }
}
