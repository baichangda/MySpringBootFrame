create table IF NOT EXISTS t_sys_role_menu
(
   role_code            varchar(50) comment '关联角色编码',
   menu_id              bigint comment '关联菜单id',
   primary key (role_code,menu_id)
);

alter table t_sys_role_menu comment '角色与菜单关联关系表';