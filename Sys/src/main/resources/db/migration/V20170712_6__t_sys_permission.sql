create table IF NOT EXISTS t_sys_role_permission
(
   id                   bigint unsigned not null auto_increment comment 'id',
      name                 varchar(20) comment '角色名称',
      code                 varchar(100) comment '编码',
      remark               varchar(256) comment '备注',
      create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
      create_user_id       bigint comment '创建人id',
      update_time          timestamp NULL comment '更新时间',
      update_user_id       bigint comment '更新人id',
   role_id              bigint comment '关联角色id',
   primary key (id)
);

alter table t_sys_role_permission comment '角色与权限关系表';