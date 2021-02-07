create table "public"."t_sys_menu"
(
    "id"               serial8     not null,
    "parent_id"        int8,
    "name"             varchar(50) not null,
    "url"              varchar(256),
    "icon"             varchar(256),
    "order_num"        int4        not null,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    "create_ip"        varchar(50),
    "update_ip"        varchar(50),
    constraint "t_sys_menu_pkey" primary key ("id")
);

comment on column "public"."t_sys_menu"."id" is '主键';

comment on column "public"."t_sys_menu"."parent_id" is '父菜单id';

comment on column "public"."t_sys_menu"."name" is '菜单名称';

comment on column "public"."t_sys_menu"."url" is 'url地址';

comment on column "public"."t_sys_menu"."icon" is '图标';

comment on column "public"."t_sys_menu"."order_num" is '排序';

comment on column "public"."t_sys_menu"."create_time" is '创建时间';

comment on column "public"."t_sys_menu"."create_user_id" is '创建人id';

comment on column "public"."t_sys_menu"."create_user_name" is '创建人姓名';

comment on column "public"."t_sys_menu"."update_time" is '更新时间';

comment on column "public"."t_sys_menu"."update_user_id" is '更新人id';

comment on column "public"."t_sys_menu"."update_user_name" is '更新人姓名';

comment on column "public"."t_sys_menu"."create_ip" is '创建ip地址';

comment on column "public"."t_sys_menu"."update_ip" is '更新ip地址';

comment on table "public"."t_sys_menu" is '菜单表';