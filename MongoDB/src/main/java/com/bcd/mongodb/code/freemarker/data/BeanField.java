package com.bcd.mongodb.code.freemarker.data;

import org.apache.commons.lang3.StringUtils;

public class BeanField {
    private String name;
    private String type;
    private String comment;
    private String commentPre;

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
        //处理字段注释前缀
        if(StringUtils.isNoneEmpty(comment)){
            if(comment.contains("(")){
                this.commentPre= comment.substring(0,comment.indexOf('('));
            }else{
                this.commentPre= comment;
            }
        }
    }

    public String getCommentPre() {
        return commentPre;
    }

    public void setCommentPre(String commentPre) {
        this.commentPre = commentPre;
    }
}
