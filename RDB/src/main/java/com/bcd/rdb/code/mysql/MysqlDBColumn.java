package com.bcd.rdb.code.mysql;

import com.bcd.rdb.code.data.BeanField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/31.
 */
@Accessors(chain = true)
@Getter
@Setter
public class MysqlDBColumn {

    public final static Map<String, String> DB_TYPE_TO_JAVA_TYPE = new HashMap<>();

    static {
        DB_TYPE_TO_JAVA_TYPE.put("decimal", "BigDecimal");
        DB_TYPE_TO_JAVA_TYPE.put("tinyint", "Byte");
        DB_TYPE_TO_JAVA_TYPE.put("smallint", "Short");
        DB_TYPE_TO_JAVA_TYPE.put("bigint", "Long");
        DB_TYPE_TO_JAVA_TYPE.put("varchar", "String");
        DB_TYPE_TO_JAVA_TYPE.put("int", "Integer");
        DB_TYPE_TO_JAVA_TYPE.put("float", "Float");
        DB_TYPE_TO_JAVA_TYPE.put("double", "Double");
        DB_TYPE_TO_JAVA_TYPE.put("timestamp", "Date");
        DB_TYPE_TO_JAVA_TYPE.put("datetime", "Date");
        DB_TYPE_TO_JAVA_TYPE.put("date", "Date");

    }

    Logger logger = LoggerFactory.getLogger(MysqlDBColumn.class);
    private String name;
    private String type;
    private String comment;
    private String isNull;
    private Integer strLen;

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
        beanField.setName(jName);
        beanField.setType(javaType);
        beanField.setComment(comment);
        beanField.setNullable(!isNull.equals("NO"));
        beanField.setStrLen(strLen);
        return beanField;
    }
}
