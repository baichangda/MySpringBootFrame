create table IF NOT EXISTS t_sys_org
(
   id                   bigint unsigned not null auto_increment comment '主键',
   parent_id            bigint comment '父组织id',
   code                 varchar(100) not null comment '组织层级编码(格式为1_2_3_,必须以_结尾)',
   name                 varchar(50) not null comment '组织名称',
   address              varchar(256) comment '地址',
   phone                varchar(11) comment '电话',
   remark               varchar(256) comment '备注',
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

alter table t_sys_org comment '组织机构基础信息表';