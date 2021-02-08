CREATE TABLE "public"."t_sys_permission"
(
    "id"               serial8     NOT NULL,
    "name"             varchar(20) NOT NULL,
    "code"             varchar(50) NOT NULL,
    "remark"           varchar(256),
    "role_id"          int8,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    "create_ip"        varchar(50),
    "update_ip"        varchar(50),
    CONSTRAINT "t_sys_permission_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."t_sys_permission"."id" IS 'id';

COMMENT ON COLUMN "public"."t_sys_permission"."name" IS '角色名称';

COMMENT ON COLUMN "public"."t_sys_permission"."code" IS '编码';

COMMENT ON COLUMN "public"."t_sys_permission"."remark" IS '备注';

COMMENT ON COLUMN "public"."t_sys_permission"."role_id" IS '关联角色id';

COMMENT ON COLUMN "public"."t_sys_permission"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."t_sys_permission"."create_user_id" IS '创建人id';

COMMENT ON COLUMN "public"."t_sys_permission"."create_user_name" IS '创建人姓名';

COMMENT ON COLUMN "public"."t_sys_permission"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."t_sys_permission"."update_user_id" IS '更新人id';

COMMENT ON COLUMN "public"."t_sys_permission"."update_user_name" IS '更新人姓名';

COMMENT ON COLUMN "public"."t_sys_permission"."create_ip" IS '创建ip地址';

COMMENT ON COLUMN "public"."t_sys_permission"."update_ip" IS '更新ip地址';

COMMENT ON TABLE "public"."t_sys_permission" IS '角色与权限关系表';