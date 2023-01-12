package com.bcd.base.support_jdbc.code.pgsql;

import com.bcd.base.support_jdbc.code.data.BeanField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/31.
 */
public class PgsqlDBColumn {

    public final static Map<String, String> DB_TYPE_TO_JAVA_TYPE = new HashMap<>();

    static {
        DB_TYPE_TO_JAVA_TYPE.put("numeric", "BigDecimal");
        DB_TYPE_TO_JAVA_TYPE.put("int8", "Long");
        DB_TYPE_TO_JAVA_TYPE.put("varchar", "String");
        DB_TYPE_TO_JAVA_TYPE.put("int2", "Integer");
        DB_TYPE_TO_JAVA_TYPE.put("int4", "Integer");
        DB_TYPE_TO_JAVA_TYPE.put("float4", "Float");
        DB_TYPE_TO_JAVA_TYPE.put("float8", "Double");
        DB_TYPE_TO_JAVA_TYPE.put("timestamp", "Date");
        DB_TYPE_TO_JAVA_TYPE.put("date", "Date");

    }

    Logger logger = LoggerFactory.getLogger(PgsqlDBColumn.class);
    public String name;
    public String type;
    public String comment;
    public String isNull;
    public Integer strLen;

    public BeanField toBeanField() {
        String javaType = DB_TYPE_TO_JAVA_TYPE.get(type);
        if (javaType == null) {

            return null;
        }
        String jName = name;
        int curIndex;
        while ((curIndex = jName.indexOf('_')) != -1) {
            jName = jName.substring(0, curIndex) +
                    jName.substring(curIndex + 1, curIndex + 2).toUpperCase() +
                    jName.substring(curIndex + 2);
        }
        BeanField beanField = new BeanField();
        beanField.name = jName;
        beanField.type = javaType;
        beanField.setComment(comment);
        beanField.nullable = !isNull.equals("NO");
        beanField.strLen = strLen;
        return beanField;
    }
}
