create table IF NOT EXISTS t_sys_user_role
(
   id                   bigint unsigned not null auto_increment comment 'id',
   user_id              bigint comment '关联用户id',
   role_code            varchar(50) comment '关联角色编码',
   primary key (id)
);

alter table t_sys_user_role comment '用户与角色关联关系表';