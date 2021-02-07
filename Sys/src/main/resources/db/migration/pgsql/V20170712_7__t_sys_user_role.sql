create table "public"."t_sys_user_role"
(
    "user_id"   int8        not null,
    "role_code" varchar(50) not null,
    constraint "t_sys_user_role_pkey" primary key ("user_id", "role_code")
);

comment on column "public"."t_sys_user_role"."user_id" is '关联用户id';

comment on column "public"."t_sys_user_role"."role_code" is '关联角色编码';

comment on table "public"."t_sys_user_role" is '用户与角色关联关系表';