create table "public"."t_sys_user"
(
    "id"               serial8      not null,
    "username"         varchar(50)  not null,
    "password"         varchar(100) not null,
    "email"            varchar(100),
    "phone"            varchar(11),
    "real_name"        varchar(50),
    "sex"              varchar(5),
    "birthday"         timestamp(6),
    "card_number"      varchar(20),
    "status"           int4         not null,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    "create_ip"        varchar(50),
    "update_ip"        varchar(50),
    constraint "t_sys_user_pkey" primary key ("id")
);

comment on column "public"."t_sys_user"."id" is '主键';

comment on column "public"."t_sys_user"."username" is '用户名';

comment on column "public"."t_sys_user"."password" is '密码';

comment on column "public"."t_sys_user"."email" is '邮箱';

comment on column "public"."t_sys_user"."phone" is '手机号';

comment on column "public"."t_sys_user"."real_name" is '真实姓名';

comment on column "public"."t_sys_user"."sex" is '性别';

comment on column "public"."t_sys_user"."birthday" is '生日';

comment on column "public"."t_sys_user"."card_number" is '身份证号';

comment on column "public"."t_sys_user"."status" is '是否可用(0:禁用,1:可用)';

comment on column "public"."t_sys_user"."create_time" is '创建时间';

comment on column "public"."t_sys_user"."create_user_id" is '创建人id';

comment on column "public"."t_sys_user"."create_user_name" is '创建人姓名';

comment on column "public"."t_sys_user"."update_time" is '更新时间';

comment on column "public"."t_sys_user"."update_user_id" is '更新人id';

comment on column "public"."t_sys_user"."update_user_name" is '更新人姓名';

comment on column "public"."t_sys_user"."create_ip" is '创建ip地址';

comment on column "public"."t_sys_user"."update_ip" is '更新ip地址';

comment on table "public"."t_sys_user" is '用户基础信息表';