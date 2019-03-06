package com.bcd.rdb.code.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/7/31.
 */
public class DBColumn {
    Logger logger= LoggerFactory.getLogger(DBColumn.class);
    private String name;
    private String type;
    private String comment;
    private String isNull;
    private Integer strLen;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public Integer getStrLen() {
        return strLen;
    }

    public void setStrLen(Integer strLen) {
        this.strLen = strLen;
    }

    public JavaColumn toJavaColumn(){
        String javaType=CodeConst.DB_TYPE_TO_JAVA_TYPE.get(type);
        if(javaType==null){
            logger.info("不支持[name:"+name+"] [type:"+type+"]类型数据库字段,忽略此字段!");
            return null;
        }
        String jName=name;
        int curIndex;
        while((curIndex=jName.indexOf('_'))!=-1){
            jName=jName.substring(0,curIndex)+
                    jName.substring(curIndex+1,curIndex+2).toUpperCase()+
                    jName.substring(curIndex+2);
        }
        JavaColumn javaColumn=new JavaColumn();
        javaColumn.setName(jName);
        javaColumn.setType(javaType);
        javaColumn.setComment(comment);
        javaColumn.setNull(!isNull.equals("NO"));
        javaColumn.setStrLen(strLen);
        return javaColumn;
    }
}
