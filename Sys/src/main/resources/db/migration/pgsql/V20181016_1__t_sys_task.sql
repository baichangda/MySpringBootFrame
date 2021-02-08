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
    "create_ip"        varchar(50),
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

COMMENT ON COLUMN "public"."t_sys_task"."create_ip" IS '创建ip';

COMMENT ON TABLE "public"."t_sys_task" IS '系统任务处理表';