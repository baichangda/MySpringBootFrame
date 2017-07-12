create table IF NOT EXISTS t_sys_user
(
   id                   bigint unsigned not null auto_increment comment '用户id',
   username             varchar(50) comment '用户名',
   password             varchar(100) comment '用户名',
   email                varchar(100) comment '邮箱',
   phone                varchar(11) comment '手机号',
   real_name            varchar(50) comment '真实姓名',
   sex                  varchar(5) comment '性别',
   birthday             timestamp NULL comment '生日',
   card_number          varchar(20) comment '身份证号',
   status               int comment '是否可用（0：禁用；1：可用）',
   create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
   create_user_id       bigint comment '创建人id',
   update_time          timestamp NULL comment '更新时间',
   update_user_id       bigint comment '更新人id',
   org_id              bigint comment '关联机构id',
   primary key (id)
);

alter table t_sys_user comment '用户基础信息表';