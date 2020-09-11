create table IF NOT EXISTS t_sys_permission
(
   id                   bigint unsigned not null auto_increment comment 'id',
   name                 varchar(20) not null comment '角色名称',
   code                 varchar(50) not null comment '编码',
   remark               varchar(256) comment '备注',
   role_id              bigint comment '关联角色id',
   create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
   create_user_id       bigint comment '创建人id',
   create_user_name     varchar(50) comment '创建人姓名',
   update_time          timestamp NULL comment '更新时间',
   update_user_id       bigint comment '更新人id',
   update_user_name     varchar(50) comment '更新人姓名',
   create_ip            varchar(50) comment '创建ip地址',
   update_ip            varchar(50) comment '更新ip地址',
   primary key (id)
);

alter table t_sys_permission comment '角色与权限关系表';