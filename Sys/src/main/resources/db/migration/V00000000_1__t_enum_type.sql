create table IF NOT EXISTS t_enum_type
(
   id                   bigint unsigned not null auto_increment comment '枚举类型id',
   name                 varchar(100) comment '枚举类型名称（例如：组织类型，设备类型，菜单类型）',
   code                 varchar(100) comment '枚举类型代码（例如：type_org，type_menu）',
   remark               varchar(256) comment '备注',
   primary key (id)
);

alter table t_enum_type comment '枚举类型表';