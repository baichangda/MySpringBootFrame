package com.base.code;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/14.
 */
public class CodeConst {
    public final static Map<String,String> TYPE_MAPPING=new HashMap<>();
    public final static Map<String,String> TYPE_CONDITION_MAPPING=new HashMap<>();
    static{
        TYPE_MAPPING.put("bigint","Long");
        TYPE_MAPPING.put("varchar","String");
        TYPE_MAPPING.put("int","Integer");
        TYPE_MAPPING.put("timestamp","Date");

        TYPE_CONDITION_MAPPING.put("Long","NumberCondition");
        TYPE_CONDITION_MAPPING.put("String","StringCondition");
        TYPE_CONDITION_MAPPING.put("Integer","NumberCondition");
        TYPE_CONDITION_MAPPING.put("Date","DateCondition");
    }
}
