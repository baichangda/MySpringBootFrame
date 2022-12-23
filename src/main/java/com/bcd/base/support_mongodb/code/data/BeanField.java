package com.bcd.base.support_mongodb.code.data;

import org.apache.commons.lang3.StringUtils;

public class BeanField {
    public String name;
    public String type;
    public String comment;
    public String commentPre;

    public BeanField setComment(String comment) {
        this.comment = comment;
        //处理字段注释前缀
        if (StringUtils.isNoneEmpty(comment)) {
            if (comment.contains("(")) {
                this.commentPre = comment.substring(0, comment.indexOf('('));
            } else {
                this.commentPre = comment;
            }
        }
        return this;
    }
}
