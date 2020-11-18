CREATE TABLE "public"."t_sys_user_role" (
  "user_id" int8 NOT NULL,
  "role_code" varchar(50) NOT NULL,
  CONSTRAINT "t_sys_user_role_pkey" PRIMARY KEY ("user_id", "role_code")
)
;

COMMENT ON COLUMN "public"."t_sys_user_role"."user_id" IS '关联用户id';

COMMENT ON COLUMN "public"."t_sys_user_role"."role_code" IS '关联角色编码';

COMMENT ON TABLE "public"."t_sys_user_role" IS '用户与角色关联关系表';