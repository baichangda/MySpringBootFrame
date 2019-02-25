package com.bcd.base.config.shiro.data;

public enum NotePermission {
    USER_SEARCH("user:search","用户查询"),
    USER_EDIT("user:edit","用户维护");

    private String code;
    private String note;
    NotePermission(String code, String note){
        this.code=code;
        this.note=note;
    }

    public String getCode() {
        return code;
    }

    public String getNote() {
        return note;
    }
}
