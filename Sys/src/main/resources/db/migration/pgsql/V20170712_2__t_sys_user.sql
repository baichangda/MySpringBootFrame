
CREATE TABLE "public"."t_sys_user" (
  "id" serial8 NOT NULL,
  "username" varchar(50) NOT NULL,
  "password" varchar(100) NOT NULL,
  "email" varchar(100),
  "phone" varchar(11),
  "real_name" varchar(50),
  "sex" varchar(5),
  "birthday" timestamp(6),
  "card_number" varchar(20),
  "status" int4 NOT NULL,
  "create_time" timestamp(6),
  "create_user_id" int8,
  "create_user_name" varchar(50),
  "update_time" timestamp(6),
  "update_user_id" int8,
  "update_user_name" varchar(50),
  "create_ip" varchar(50),
  "update_ip" varchar(50),
  CONSTRAINT "t_sys_user_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON COLUMN "public"."t_sys_user"."id" IS '主键';

COMMENT ON COLUMN "public"."t_sys_user"."username" IS '用户名';

COMMENT ON COLUMN "public"."t_sys_user"."password" IS '密码';

COMMENT ON COLUMN "public"."t_sys_user"."email" IS '邮箱';

COMMENT ON COLUMN "public"."t_sys_user"."phone" IS '手机号';

COMMENT ON COLUMN "public"."t_sys_user"."real_name" IS '真实姓名';

COMMENT ON COLUMN "public"."t_sys_user"."sex" IS '性别';

COMMENT ON COLUMN "public"."t_sys_user"."birthday" IS '生日';

COMMENT ON COLUMN "public"."t_sys_user"."card_number" IS '身份证号';

COMMENT ON COLUMN "public"."t_sys_user"."status" IS '是否可用(0:禁用,1:可用)';

COMMENT ON COLUMN "public"."t_sys_user"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."t_sys_user"."create_user_id" IS '创建人id';

COMMENT ON COLUMN "public"."t_sys_user"."create_user_name" IS '创建人姓名';

COMMENT ON COLUMN "public"."t_sys_user"."update_time" IS '更新时间';

COMMENT ON COLUMN "public"."t_sys_user"."update_user_id" IS '更新人id';

COMMENT ON COLUMN "public"."t_sys_user"."update_user_name" IS '更新人姓名';

COMMENT ON COLUMN "public"."t_sys_user"."create_ip" IS '创建ip地址';

COMMENT ON COLUMN "public"."t_sys_user"."update_ip" IS '更新ip地址';

COMMENT ON TABLE "public"."t_sys_user" IS '用户基础信息表';