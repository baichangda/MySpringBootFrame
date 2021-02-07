create table "public"."t_sys_role"
(
    "id"               serial8     not null,
    "name"             varchar(20) not null,
    "code"             varchar(50) not null,
    "remark"           varchar(256),
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    "create_ip"        varchar(50),
    "update_ip"        varchar(50),
    constraint "t_sys_role_pkey" primary key ("id")
);

comment on column "public"."t_sys_role"."id" is '主键';

comment on column "public"."t_sys_role"."name" is '角色名称';

comment on column "public"."t_sys_role"."code" is '编码';

comment on column "public"."t_sys_role"."remark" is '备注';

comment on column "public"."t_sys_role"."create_time" is '创建时间';

comment on column "public"."t_sys_role"."create_user_id" is '创建人id';

comment on column "public"."t_sys_role"."create_user_name" is '创建人姓名';

comment on column "public"."t_sys_role"."update_time" is '更新时间';

comment on column "public"."t_sys_role"."update_user_id" is '更新人id';

comment on column "public"."t_sys_role"."update_user_name" is '更新人姓名';

comment on column "public"."t_sys_role"."create_ip" is '创建ip地址';

comment on column "public"."t_sys_role"."update_ip" is '更新ip地址';

comment on table "public"."t_sys_role" is '角色表';