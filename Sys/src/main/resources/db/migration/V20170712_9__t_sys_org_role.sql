create table IF NOT EXISTS t_sys_org_role
(
   id                   bigint unsigned not null auto_increment comment 'id',
   org_id               bigint comment '关联机构id',
   role_id              bigint comment '关联角色id',
   primary key (id)
);

alter table t_sys_org_role comment '角色与机构关联关系表';