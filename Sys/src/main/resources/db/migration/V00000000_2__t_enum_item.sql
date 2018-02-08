create table IF NOT EXISTS t_enum_item
(
   id                   bigint unsigned not null auto_increment comment '枚举项id',
   type_id              bigint comment '枚举类型id',
   name                 varchar(100) comment '枚举项名称(例如：OBD，按钮)',
   code                 varchar(100) comment '枚举项代码（例如：type_menu_button，type_device_obd）',
   remark               varchar(256) comment '备注',
   primary key (id)
);

alter table t_enum_item comment '枚举项表';