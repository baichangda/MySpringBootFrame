CREATE TABLE IF NOT EXISTS t_sys_menu_permission
(
    menu_id         varchar(50) COMMENT '关联菜单id',
    permission_code varchar(50) COMMENT '关联权限编码',
    PRIMARY KEY (menu_id, permission_code)
);

ALTER TABLE t_sys_menu_permission
    COMMENT '菜单与权限关联关系表';