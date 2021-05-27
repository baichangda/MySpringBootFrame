package com.bcd.base.support_rdb.code.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Accessors(chain = true)
@Getter
@Setter
public class BeanField {
    private String name;
    private String type;
    private String comment;
    private String commentPre;
    private boolean nullable = true;
    //type="String"时候才有效
    private Integer strLen;

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