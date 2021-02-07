create table if not exists t_sys_task
(
    id               bigint unsigned not null auto_increment comment '主键',
    name             varchar(50)     not null comment '任务名称',
    status           int             not null comment '任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)',
    type             int             null comment '任务类型(1:普通任务;2:文件类型任务)',
    percent          FLOAT           not null comment '任务处理进度',
    message          varchar(255)    null comment '任务信息(失败时记录失败原因)',
    stack_message    text            null comment '失败堆栈信息(失败时后台异常堆栈信息)',
    start_time       timestamp       null comment '任务开始时间',
    finish_time      timestamp       null comment '任务完成时间',
    create_time      timestamp       null default current_timestamp comment '创建时间',
    file_paths       varchar(100)    null comment '文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)',
    create_user_id   bigint comment '创建人id',
    create_user_name varchar(50) comment '创建人姓名',
    create_ip        varchar(50) comment '创建ip',
    primary key (id)
);

alter table t_sys_task
    comment '系统任务处理表';