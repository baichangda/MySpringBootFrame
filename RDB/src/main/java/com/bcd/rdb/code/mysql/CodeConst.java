package com.bcd.rdb.code.mysql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/14.
 */
public class CodeConst {
    public final static String TEMPLATE_DIR_PATH = System.getProperty("user.dir") + "/RDB/src/main/resources/template";

    public final static Map<String,String> DB_TYPE_TO_JAVA_TYPE =new HashMap<>();

    static{
        DB_TYPE_TO_JAVA_TYPE.put("decimal","BigDecimal");
        DB_TYPE_TO_JAVA_TYPE.put("tinyint","Byte");
        DB_TYPE_TO_JAVA_TYPE.put("smallint","Short");
        DB_TYPE_TO_JAVA_TYPE.put("bigint","Long");
        DB_TYPE_TO_JAVA_TYPE.put("varchar","String");
        DB_TYPE_TO_JAVA_TYPE.put("int","Integer");
        DB_TYPE_TO_JAVA_TYPE.put("float","Float");
        DB_TYPE_TO_JAVA_TYPE.put("double","Double");
        DB_TYPE_TO_JAVA_TYPE.put("timestamp","Date");
        DB_TYPE_TO_JAVA_TYPE.put("datetime","Date");
        DB_TYPE_TO_JAVA_TYPE.put("date","Date");

    }

    public final static Map<String,String> TYPE_TO_CONDITION =new HashMap<>();
    static{
        TYPE_TO_CONDITION.put("BigDecimal","NumberCondition");
        TYPE_TO_CONDITION.put("Byte","NumberCondition");
        TYPE_TO_CONDITION.put("byte","NumberCondition");
        TYPE_TO_CONDITION.put("Short","NumberCondition");
        TYPE_TO_CONDITION.put("short","NumberCondition");
        TYPE_TO_CONDITION.put("Long","NumberCondition");
        TYPE_TO_CONDITION.put("long","NumberCondition");
        TYPE_TO_CONDITION.put("String","StringCondition");
        TYPE_TO_CONDITION.put("Integer","NumberCondition");
        TYPE_TO_CONDITION.put("int","NumberCondition");
        TYPE_TO_CONDITION.put("Float","NumberCondition");
        TYPE_TO_CONDITION.put("float","NumberCondition");
        TYPE_TO_CONDITION.put("Double","NumberCondition");
        TYPE_TO_CONDITION.put("double","NumberCondition");
        TYPE_TO_CONDITION.put("Date","DateCondition");
        TYPE_TO_CONDITION.put("Boolean","BooleanCondition");
        TYPE_TO_CONDITION.put("boolean","BooleanCondition");
    }

    public final static Map<String,String> BASE_TYPE_TO_PACKAGE_TYPE =new HashMap<>();
    static{
        BASE_TYPE_TO_PACKAGE_TYPE.put("long","Long");
        BASE_TYPE_TO_PACKAGE_TYPE.put("int","Integer");
        BASE_TYPE_TO_PACKAGE_TYPE.put("float","Float");
        BASE_TYPE_TO_PACKAGE_TYPE.put("double","Double");
        BASE_TYPE_TO_PACKAGE_TYPE.put("boolean","Boolean");
        BASE_TYPE_TO_PACKAGE_TYPE.put("byte","Byte");
        BASE_TYPE_TO_PACKAGE_TYPE.put("char","Character");
        BASE_TYPE_TO_PACKAGE_TYPE.put("short","Short");
    }

    public final static Map<String,String> PACKAGE_TYPE_TO_SWAGGER_EXAMPLE =new HashMap<>();
    static{
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Integer","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("int","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Long","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("long","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Float","1.1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("float","1.1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Double","1.1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("double","1.1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Byte","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("byte","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("Short","1");
        PACKAGE_TYPE_TO_SWAGGER_EXAMPLE.put("short","1");
    }

    public final static Set<String> SUPPORT_PK_TYPE=new HashSet<>();
    static {
        SUPPORT_PK_TYPE.add("Long");
        SUPPORT_PK_TYPE.add("String");
        SUPPORT_PK_TYPE.add("Integer");
    }

    public final static Set<String> IGNORE_FIELD_NAME =new HashSet<>();
    static{
        IGNORE_FIELD_NAME.add("createTime");
        IGNORE_FIELD_NAME.add("updateTime");
        IGNORE_FIELD_NAME.add("createUserId");
        IGNORE_FIELD_NAME.add("createUserName");
        IGNORE_FIELD_NAME.add("updateUserId");
        IGNORE_FIELD_NAME.add("updateUserName");
        IGNORE_FIELD_NAME.add("createIp");
        IGNORE_FIELD_NAME.add("updateIp");
    }
}
