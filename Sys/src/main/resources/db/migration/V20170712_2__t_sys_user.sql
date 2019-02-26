create table IF NOT EXISTS t_sys_user
(
   id                   bigint unsigned not null auto_increment comment '主键',
   type                 int not null comment '类型(1:管理用户,2:企业用户)',
   org_code             varchar(100) comment '关联机构编码',
   username             varchar(50) not null comment '用户名',
   password             varchar(100) not null comment '密码',
   email                varchar(100) comment '邮箱',
   phone                varchar(11) comment '手机号',
   real_name            varchar(50) comment '真实姓名',
   sex                  varchar(5) comment '性别',
   birthday             timestamp NULL comment '生日',
   card_number          varchar(20) comment '身份证号',
   status               int not null DEFAULT 1 comment '是否可用(0:禁用,1:可用)',
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

alter table t_sys_user comment '用户基础信息表';