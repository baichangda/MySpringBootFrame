package com.bcd.rdb.condition.converter.jdbc;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.Converter;
import com.bcd.base.condition.impl.*;
import com.bcd.base.util.StringUtil;
import com.bcd.rdb.util.ConditionUtil;
import com.bcd.rdb.util.RDBUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/11.
 */
@SuppressWarnings("unchecked")
public class BooleanConditionConverter implements Converter<BooleanCondition,String>{
    @Override
    public String convert(BooleanCondition condition, Object... exts) {
        StringBuilder where=new StringBuilder();
        Object val=condition.val;
        String columnName= StringUtil.toFirstSplitWithUpperCase(condition.fieldName,'_');
        Map<String,Object> paramMap=(Map<String,Object>)exts[0];
        Map<String,Integer> paramToCount=(Map<String,Integer>)exts[1];
        String paramName= RDBUtil.generateRandomParamName(columnName,paramToCount);
        if(val!=null){
            where.append(columnName);
            where.append(" = ");
            where.append(":");
            where.append(paramName);
            paramMap.put(paramName,val);
        }
        return where.length()==0?null:where.toString();
    }

    public static void main(String[] args) {
        Condition condition=Condition.and(
                new StringCondition("a","a", StringCondition.Handler.LEFT_LIKE),
                new StringCondition("b","b", StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("c","c", StringCondition.Handler.ALL_LIKE),
                Condition.and(
                    new NumberCondition("m.i",1, NumberCondition.Handler.EQUAL),
                    new NumberCondition("h.j",12, NumberCondition.Handler.GE),
                    new DateCondition("h.fdfDsf",12, DateCondition.Handler.GE),
                    new BooleanCondition("kn.l",true),
                    new NullCondition("daf")
                ),
                Condition.or(
                        new NumberCondition("m.i",1, NumberCondition.Handler.EQUAL),
                        new NumberCondition("h.j",12, NumberCondition.Handler.GE),
                        new DateCondition("h.fdfdsf",12, DateCondition.Handler.GE),
                        new BooleanCondition("kn.l",true),
                        new NullCondition("daf"),
                        Condition.and(
                                new NumberCondition("m.i",1, NumberCondition.Handler.EQUAL),
                                new NumberCondition("h.j",12, NumberCondition.Handler.GE),
                                new DateCondition("h.fdfdsf",12, DateCondition.Handler.GE),
                                new BooleanCondition("kn.l",true),
                                new NullCondition("daf"),
                                new StringCondition("g.i", Arrays.asList("a","b"), StringCondition.Handler.IN)
                        )
                )
        );
        Map<String,Object> paramMap=new HashMap<>();
        String sql=ConditionUtil.convertCondition(condition,paramMap);
        System.out.println(sql);
    }
}
