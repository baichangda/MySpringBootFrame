package com.base.code;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/31.
 */
public class DBColumn {
    private final static Map<String,String> TYPE_MAPPING=new HashMap<>();
    static{
        TYPE_MAPPING.put("bigint","Long");
        TYPE_MAPPING.put("varchar","String");
        TYPE_MAPPING.put("int","Integer");
        TYPE_MAPPING.put("timestamp","Date");
    }
    private String name;
    private String type;
    private String comment;

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

    public JavaColumn toJavaColumn(){
        JavaColumn javaColumn=new JavaColumn();
        String jName=name;
        int curIndex;
        while((curIndex=jName.indexOf("_"))!=-1){
            jName=jName.substring(0,curIndex)+
                    jName.substring(curIndex+1,curIndex+2).toUpperCase()+
                    jName.substring(curIndex+2);
        }
        javaColumn.setName(jName);
        javaColumn.setType(TYPE_MAPPING.get(type));
        javaColumn.setComment(comment);
        return javaColumn;
    }
}
