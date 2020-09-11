CREATE TABLE "public"."t_sys_menu" (
  "id" int8 NOT NULL,
  "parent_id" int8,
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "url" varchar(256) COLLATE "pg_catalog"."default",
  "icon" varchar(256) COLLATE "pg_catalog"."default",
  "order_num" int4 NOT NULL,
  "create_time" timestamp(6),
  "create_user_id" int8,
  "create_user_name" varchar(50) COLLATE "pg_catalog"."default",
  "update_time" timestamp(6),
  "update_user_id" int8,
  "update_user_name" varchar(50) COLLATE "pg_catalog"."default",
  "create_ip" varchar(50) COLLATE "pg_catalog"."default",
  "update_ip" varchar(50) COLLATE "pg_catalog"."default",
  CONSTRAINT "t_sys_menu_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON COLUMN "public"."t_sys_menu"."id" IS '主键';

COMMENT ON COLUMN "public"."t_sys_menu"."parent_id" IS '父菜单id';

COMMENT ON COLUMN "public"."t_sys_menu"."name" IS '菜单名称';

COMMENT ON COLUMN "public"."t_sys_menu"."url" IS 'url地址';

COMMENT ON COLUMN "public"."t_sys_menu"."icon" IS '图标';

COMMENT ON COLUMN "public"."t_sys_menu"."order_num" IS '排序';

COMMENT ON COLUMN "public"."t_sys_menu"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."t_sys_menu"."create_user_id" IS '创建人id';

COMMENT ON COLUMN "public"."t_sys_menu"."create_user_name" IS '创建人姓名';

COMMENT ON COLUMN "public"."t_sys_menu"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."t_sys_menu"."update_user_id" IS '更新人id';

COMMENT ON COLUMN "public"."t_sys_menu"."update_user_name" IS '更新人姓名';

COMMENT ON COLUMN "public"."t_sys_menu"."create_ip" IS '创建ip地址';

COMMENT ON COLUMN "public"."t_sys_menu"."update_ip" IS '更新ip地址';

COMMENT ON TABLE "public"."t_sys_menu" IS '菜单表';