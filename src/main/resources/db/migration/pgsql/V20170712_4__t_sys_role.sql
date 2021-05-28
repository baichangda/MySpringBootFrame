CREATE TABLE "public"."t_sys_role"
(
    "id"               serial8     NOT NULL,
    "name"             varchar(20) NOT NULL,
    "code"             varchar(50) NOT NULL,
    "remark"           varchar(256),
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    CONSTRAINT "t_sys_role_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."t_sys_role"."id" IS '主键';

COMMENT ON COLUMN "public"."t_sys_role"."name" IS '角色名称';

COMMENT ON COLUMN "public"."t_sys_role"."code" IS '编码';

COMMENT ON COLUMN "public"."t_sys_role"."remark" IS '备注';

COMMENT ON COLUMN "public"."t_sys_role"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."t_sys_role"."create_user_id" IS '创建人id';

COMMENT ON COLUMN "public"."t_sys_role"."create_user_name" IS '创建人姓名';

COMMENT ON COLUMN "public"."t_sys_role"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."t_sys_role"."update_user_id" IS '更新人id';

COMMENT ON COLUMN "public"."t_sys_role"."update_user_name" IS '更新人姓名';

COMMENT ON TABLE "public"."t_sys_role" IS '角色表';