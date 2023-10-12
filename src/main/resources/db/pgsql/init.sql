CREATE TABLE "public"."t_sys_user"
(
    "id"               serial8      NOT NULL,
    "username"         varchar(50)  NOT NULL,
    "password"         varchar(100) NOT NULL,
    "email"            varchar(100),
    "phone"            varchar(11),
    "real_name"        varchar(50),
    "sex"              varchar(5),
    "birthday"         timestamp(6),
    "card_number"      varchar(20),
    "status"           int4         NOT NULL,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    CONSTRAINT "t_sys_user_pkey" PRIMARY KEY ("id")
);

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

COMMENT ON TABLE "public"."t_sys_user" IS '用户基础信息表';



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


CREATE TABLE "public"."t_sys_menu"
(
    "id"               serial8     NOT NULL,
    "parent_id"        int8,
    "name"             varchar(50) NOT NULL,
    "url"              varchar(256),
    "icon"             varchar(256),
    "order_num"        int4        NOT NULL,
    "create_time"      timestamp(6),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "update_time"      timestamp(6),
    "update_user_id"   int8,
    "update_user_name" varchar(50),
    CONSTRAINT "t_sys_menu_pkey" PRIMARY KEY ("id")
);

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

COMMENT ON TABLE "public"."t_sys_menu" IS '菜单表';


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

COMMENT ON TABLE "public"."t_sys_permission" IS '角色与权限关系表';


CREATE TABLE "public"."t_sys_user_role"
(
    "user_id"   int8        NOT NULL,
    "role_code" varchar(50) NOT NULL,
    CONSTRAINT "t_sys_user_role_pkey" PRIMARY KEY ("user_id", "role_code")
);

COMMENT ON COLUMN "public"."t_sys_user_role"."user_id" IS '关联用户id';

COMMENT ON COLUMN "public"."t_sys_user_role"."role_code" IS '关联角色编码';

COMMENT ON TABLE "public"."t_sys_user_role" IS '用户与角色关联关系表';


CREATE TABLE "public"."t_sys_role_menu"
(
    "role_code" varchar(50) NOT NULL,
    "menu_id"   int8        NOT NULL,
    CONSTRAINT "t_sys_role_menu_pkey" PRIMARY KEY ("role_code", "menu_id")
);

COMMENT ON COLUMN "public"."t_sys_role_menu"."role_code" IS '关联角色编码';

COMMENT ON COLUMN "public"."t_sys_role_menu"."menu_id" IS '关联菜单id';

COMMENT ON TABLE "public"."t_sys_role_menu" IS '角色与菜单关联关系表';



CREATE TABLE "public"."t_sys_menu_permission"
(
    "menu_id"         varchar(50) NOT NULL,
    "permission_code" varchar(50) NOT NULL,
    CONSTRAINT "t_sys_menu_permission_pkey" PRIMARY KEY ("menu_id", "permission_code")
);

COMMENT ON COLUMN "public"."t_sys_menu_permission"."menu_id" IS '关联菜单id';

COMMENT ON COLUMN "public"."t_sys_menu_permission"."permission_code" IS '关联权限编码';

COMMENT ON TABLE "public"."t_sys_menu_permission" IS '菜单与权限关联关系表';



CREATE TABLE "public"."t_sys_task"
(
    "id"               serial8     NOT NULL,
    "name"             varchar(50) NOT NULL,
    "status"           int4        NOT NULL,
    "type"             int4,
    "percent"          float4      NOT NULL,
    "message"          varchar(255),
    "stack_message"    text,
    "start_time"       timestamp(6),
    "finish_time"      timestamp(6),
    "create_time"      timestamp(6),
    "file_paths"       varchar(100),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    CONSTRAINT "t_sys_task_pkey" PRIMARY KEY ("id")
);

COMMENT ON COLUMN "public"."t_sys_task"."id" IS '主键';

COMMENT ON COLUMN "public"."t_sys_task"."name" IS '任务名称';

COMMENT ON COLUMN "public"."t_sys_task"."status" IS '任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)';

COMMENT ON COLUMN "public"."t_sys_task"."type" IS '任务类型(1:普通任务;2:文件类型任务)';

COMMENT ON COLUMN "public"."t_sys_task"."percent" IS '任务处理进度';

COMMENT ON COLUMN "public"."t_sys_task"."message" IS '任务信息(失败时记录失败原因)';

COMMENT ON COLUMN "public"."t_sys_task"."stack_message" IS '失败堆栈信息(失败时后台异常堆栈信息)';

COMMENT ON COLUMN "public"."t_sys_task"."start_time" IS '任务开始时间';

COMMENT ON COLUMN "public"."t_sys_task"."finish_time" IS '任务完成时间';

COMMENT ON COLUMN "public"."t_sys_task"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."t_sys_task"."file_paths" IS '文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)';

COMMENT ON COLUMN "public"."t_sys_task"."create_user_id" IS '创建人id';

COMMENT ON COLUMN "public"."t_sys_task"."create_user_name" IS '创建人姓名';

COMMENT ON TABLE "public"."t_sys_task" IS '系统任务处理表';