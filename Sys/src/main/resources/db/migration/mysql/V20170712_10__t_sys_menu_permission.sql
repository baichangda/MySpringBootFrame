create table if not exists t_sys_menu_permission
(
    menu_id         varchar(50) comment '关联菜单id',
    permission_code varchar(50) comment '关联权限编码',
    primary key (menu_id, permission_code)
);

alter table t_sys_menu_permission
    comment '菜单与权限关联关系表';