CREATE TABLE "public"."t_sys_menu_permission" (
  "menu_id" varchar(50) NOT NULL,
  "permission_code" varchar(50) NOT NULL,
  CONSTRAINT "t_sys_menu_permission_pkey" PRIMARY KEY ("menu_id", "permission_code")
)
;

COMMENT ON COLUMN "public"."t_sys_menu_permission"."menu_id" IS '关联菜单id';

COMMENT ON COLUMN "public"."t_sys_menu_permission"."permission_code" IS '关联权限编码';

COMMENT ON TABLE "public"."t_sys_menu_permission" IS '菜单与权限关联关系表';