package com.bcd.base.support_mongodb.condition.converter;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.ConcatCondition;
import com.bcd.base.support_mongodb.util.ConditionUtil;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Objects;


/**
 * Created by Administrator on 2017/9/15.
 */
public class ConcatConditionConverter implements Converter<ConcatCondition, Criteria> {
    @Override
    public Criteria convert(ConcatCondition condition, Object... exts) {
        List<Condition> conditionList = condition.conditions;
        ConcatCondition.ConcatWay concatWay = condition.concatWay;
        Criteria[] criterias = conditionList.stream().map(ConditionUtil::convertCondition).filter(Objects::nonNull).toArray(Criteria[]::new);
        if (criterias.length == 0) {
            return null;
        }
        switch (concatWay) {
            case AND: {
                return new Criteria().andOperator(criterias);
            }
            case OR: {
                return new Criteria().orOperator(criterias);
            }
            default: {
                return null;
            }
        }
    }
}
