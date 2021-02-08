CREATE TABLE IF NOT EXISTS t_sys_task
(
    id               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name             varchar(50)     NOT NULL COMMENT '任务名称',
    status           int             NOT NULL COMMENT '任务状态(1:等待中;2:执行中;3:任务被终止;4:已完成;5:执行失败)',
    type             int             NULL COMMENT '任务类型(1:普通任务;2:文件类型任务)',
    percent          FLOAT           NOT NULL COMMENT '任务处理进度',
    message          varchar(255)    NULL COMMENT '任务信息(失败时记录失败原因)',
    stack_message    text            NULL COMMENT '失败堆栈信息(失败时后台异常堆栈信息)',
    start_time       timestamp       NULL COMMENT '任务开始时间',
    finish_time      timestamp       NULL COMMENT '任务完成时间',
    create_time      timestamp       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    file_paths       varchar(100)    NULL COMMENT '文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)',
    create_user_id   bigint COMMENT '创建人id',
    create_user_name varchar(50) COMMENT '创建人姓名',
    create_ip        varchar(50) COMMENT '创建ip',
    PRIMARY KEY (id)
);

ALTER TABLE t_sys_task
    COMMENT '系统任务处理表';