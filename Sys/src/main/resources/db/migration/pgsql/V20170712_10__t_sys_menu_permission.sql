create table "public"."t_sys_menu_permission"
(
    "menu_id"         varchar(50) not null,
    "permission_code" varchar(50) not null,
    constraint "t_sys_menu_permission_pkey" primary key ("menu_id", "permission_code")
);

comment on column "public"."t_sys_menu_permission"."menu_id" is '关联菜单id';

comment on column "public"."t_sys_menu_permission"."permission_code" is '关联权限编码';

comment on table "public"."t_sys_menu_permission" is '菜单与权限关联关系表';