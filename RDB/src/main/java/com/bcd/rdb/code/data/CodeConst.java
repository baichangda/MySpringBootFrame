package com.bcd.rdb.code.data;

import freemarker.template.Configuration;
import freemarker.template.Version;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/14.
 */
public class CodeConst {
    public final static Version FREEMARKER_VERSION= Configuration.VERSION_2_3_30;

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
