create table "public"."t_sys_role_menu"
(
    "role_code" varchar(50) not null,
    "menu_id"   int8        not null,
    constraint "t_sys_role_menu_pkey" primary key ("role_code", "menu_id")
);

comment on column "public"."t_sys_role_menu"."role_code" is '关联角色编码';

comment on column "public"."t_sys_role_menu"."menu_id" is '关联菜单id';

comment on table "public"."t_sys_role_menu" is '角色与菜单关联关系表';