create table "public"."t_sys_permission"
(
    "id"               serial8     not null,
    "name"             varchar(20) not null,
    "code"             varchar(50) not null,
    "remark"           varchar(256),
    "role_id"          int8,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    "create_ip"        varchar(50),
    "update_ip"        varchar(50),
    constraint "t_sys_permission_pkey" primary key ("id")
);

comment on column "public"."t_sys_permission"."id" is 'id';

comment on column "public"."t_sys_permission"."name" is '角色名称';

comment on column "public"."t_sys_permission"."code" is '编码';

comment on column "public"."t_sys_permission"."remark" is '备注';

comment on column "public"."t_sys_permission"."role_id" is '关联角色id';

comment on column "public"."t_sys_permission"."create_time" is '创建时间';

comment on column "public"."t_sys_permission"."create_user_id" is '创建人id';

comment on column "public"."t_sys_permission"."create_user_name" is '创建人姓名';

comment on column "public"."t_sys_permission"."update_time" is '更新时间';

comment on column "public"."t_sys_permission"."update_user_id" is '更新人id';

comment on column "public"."t_sys_permission"."update_user_name" is '更新人姓名';

comment on column "public"."t_sys_permission"."create_ip" is '创建ip地址';

comment on column "public"."t_sys_permission"."update_ip" is '更新ip地址';

comment on table "public"."t_sys_permission" is '角色与权限关系表';