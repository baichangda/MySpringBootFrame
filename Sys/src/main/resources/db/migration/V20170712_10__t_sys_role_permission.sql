create table IF NOT EXISTS t_sys_role_menu
(
   id                   bigint unsigned not null auto_increment comment 'id',
   role_code            varchar(50) comment '关联角色编码',
   permission_code      varchar(50) comment '关联权限编码',
   primary key (id)
);

alter table t_sys_role_menu comment '角色与权限关联关系表';