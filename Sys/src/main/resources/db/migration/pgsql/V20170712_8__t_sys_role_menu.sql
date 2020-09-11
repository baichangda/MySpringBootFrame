CREATE TABLE "public"."t_sys_role_menu" (
  "role_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "menu_id" int8 NOT NULL,
  CONSTRAINT "t_sys_role_menu_pkey" PRIMARY KEY ("role_code", "menu_id")
)
;

ALTER TABLE "public"."t_sys_role_menu"
  OWNER TO "baichangda";

COMMENT ON COLUMN "public"."t_sys_role_menu"."role_code" IS '关联角色编码';

COMMENT ON COLUMN "public"."t_sys_role_menu"."menu_id" IS '关联菜单id';

COMMENT ON TABLE "public"."t_sys_role_menu" IS '角色与菜单关联关系表';