CREATE TABLE IF NOT EXISTS t_sys_role_menu
(
    role_code varchar(50) COMMENT '关联角色编码',
    menu_id   bigint COMMENT '关联菜单id',
    PRIMARY KEY (role_code, menu_id)
);

ALTER TABLE t_sys_role_menu
    COMMENT '角色与菜单关联关系表';