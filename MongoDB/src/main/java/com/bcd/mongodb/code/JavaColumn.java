package com.bcd.mongodb.code;

/**
 * Created by Administrator on 2017/10/10.
 */
public class JavaColumn {
    private String name;
    private String type;
    private String comment="";
    public JavaColumn(){

    }
    public JavaColumn(String name, String type) {
        this.name = name;
        this.type = type;
    }

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
}
