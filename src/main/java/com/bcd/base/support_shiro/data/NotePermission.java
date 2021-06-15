package com.bcd.base.support_shiro.data;

public enum NotePermission {
    user_search("user:search", "用户查询"),
    user_edit("user:edit", "用户维护"),
    user_runAs("user:runAs", "用户身份授权"),

    role_search("role:search", "角色查询"),
    role_edit("role:edit", "角色维护"),

    menu_search("menu:search", "菜单查询"),
    menu_authorize("menu:authorize", "菜单授权"),
    menu_edit("menu:edit", "菜单维护"),

    permission_search("permission:search", "权限查询"),
    permission_edit("permission:edit", "权限维护"),

    sysTask_search("sysTask:search", "系统任务查询"),
    sysTask_stop("sysTask:stop", "系统任务停止"),
    ;

    private String code;
    private String note;

    NotePermission(String code, String note) {
        this.code = code;
        this.note = note;
    }

    public String getCode() {
        return code;
    }

    public String getNote() {
        return note;
    }
}
