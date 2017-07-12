create table IF NOT EXISTS t_sys_org
(
   id                   bigint unsigned not null auto_increment comment 'id',
   parent_id            bigint comment '父组织id',
   org_item_id          bigint comment '组织类型枚举项id',
   name                 varchar(50) comment '组织名称',
   address              varchar(256) comment '地址',
   phone                varchar(11) comment '电话',
   remark               varchar(256) comment '备注',
   create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
   create_user_id       bigint comment '创建人id',
   update_time          timestamp NULL comment '更新时间',
   update_user_id       bigint comment '更新人id',
   primary key (id)
);

alter table t_sys_org comment '组织机构基础信息表';