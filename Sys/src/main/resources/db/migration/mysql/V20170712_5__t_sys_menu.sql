create table IF NOT EXISTS t_sys_menu
(
   id                   bigint unsigned not null auto_increment comment '主键',
   parent_id            bigint comment '父菜单id',
   name                 varchar(50) not null comment '菜单名称',
   url                  varchar(256) comment 'url地址',
   icon                 varchar(256) comment '图标',
   order_num            int not null comment '排序',
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

alter table t_sys_menu comment '菜单表';