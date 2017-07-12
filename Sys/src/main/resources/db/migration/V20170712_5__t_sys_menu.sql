create table IF NOT EXISTS t_sys_menu
(
   id                   bigint unsigned not null auto_increment comment 'id',
   parent_id            bigint comment '父菜单id',
   name                 varchar(50) comment '菜单名称',
   menu_item_id         bigint comment '菜单类型枚举项id',
   url                  varchar(256) comment 'url地址',
   icon                 varchar(256) comment '图标',
   order_num            int comment '排序',
   create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
   create_user_id       bigint comment '创建人id',
   update_time          timestamp NULL comment '更新时间',
   update_user_id       bigint comment '更新人id',
   primary key (id)
);

alter table t_sys_menu comment '菜单表';