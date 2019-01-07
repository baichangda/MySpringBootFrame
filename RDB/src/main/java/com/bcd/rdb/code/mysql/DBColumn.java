package com.bcd.rdb.code.mysql;

import com.bcd.base.exception.BaseRuntimeException;

/**
 * Created by Administrator on 2017/7/31.
 */
public class DBColumn {
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
        JavaColumn javaColumn=new JavaColumn();
        String jName=name;
        int curIndex;
        while((curIndex=jName.indexOf('_'))!=-1){
            jName=jName.substring(0,curIndex)+
                    jName.substring(curIndex+1,curIndex+2).toUpperCase()+
                    jName.substring(curIndex+2);
        }
        javaColumn.setName(jName);
        String javaType=CodeConst.DB_TYPE_TO_JAVA_TYPE.get(type);
        if(javaType==null){
            throw BaseRuntimeException.getException("不支持[name:"+name+"] [type:"+type+"]类型数据库字段,忽略此字段!");
        }
        javaColumn.setType(javaType);
        javaColumn.setComment(comment);
        javaColumn.setNull(!isNull.equals("NO"));
        javaColumn.setStrLen(strLen);
        return javaColumn;
    }
}
