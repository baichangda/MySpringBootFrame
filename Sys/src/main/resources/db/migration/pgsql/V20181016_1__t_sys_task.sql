create table "public"."t_sys_task"
(
    "id"               serial8     not null,
    "name"             varchar(50) not null,
    "status"           int4        not null,
    "type"             int4,
    "percent"          float4      not null,
    "message"          varchar(255),
    "stack_message"    text,
    "start_time"       timestamp(6),
    "finish_time"      timestamp(6),
    "create_time"      timestamp(6),
    "file_paths"       varchar(100),
    "create_user_id"   int8,
    "create_user_name" varchar(50),
    "create_ip"        varchar(50),
    constraint "t_sys_task_pkey" primary key ("id")
);

comment on column "public"."t_sys_task"."id" is '主键';

comment on column "public"."t_sys_task"."name" is '任务名称';

comment on column "public"."t_sys_task"."status" is '任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)';

comment on column "public"."t_sys_task"."type" is '任务类型(1:普通任务;2:文件类型任务)';

comment on column "public"."t_sys_task"."percent" is '任务处理进度';

comment on column "public"."t_sys_task"."message" is '任务信息(失败时记录失败原因)';

comment on column "public"."t_sys_task"."stack_message" is '失败堆栈信息(失败时后台异常堆栈信息)';

comment on column "public"."t_sys_task"."start_time" is '任务开始时间';

comment on column "public"."t_sys_task"."finish_time" is '任务完成时间';

comment on column "public"."t_sys_task"."create_time" is '创建时间';

comment on column "public"."t_sys_task"."file_paths" is '文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)';

comment on column "public"."t_sys_task"."create_user_id" is '创建人id';

comment on column "public"."t_sys_task"."create_user_name" is '创建人姓名';

comment on column "public"."t_sys_task"."create_ip" is '创建ip';

comment on table "public"."t_sys_task" is '系统任务处理表';