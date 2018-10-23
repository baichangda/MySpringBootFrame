create table IF NOT EXISTS t_sys_task
(
   id                   bigint unsigned not null auto_increment comment '主键',
   name                 varchar(50) not null comment '任务名称',
   status               int not null comment '任务状态(1:等待中;2:执行中;2:任务被终止;2:已完成;3:执行失败;)',
   type                 tinyint not null comment '任务类型(1:普通任务;2:文件类型任务)',
   remark               varchar(255) not null comment '备注(失败时记录失败原因)',
   finish_time          timestamp NULL comment '任务完成时间',
   create_time          timestamp NULL default CURRENT_TIMESTAMP comment '创建时间',
   file_paths           varchar(100) not null comment '文件路径(如果是生成文件的任务,存储的是文件路径;可以存储多个,以;分割)',
   create_user_id       bigint comment '创建人id',
   create_user_name     varchar(50) comment '创建人姓名',
   create_ip            varchar(50) comment '创建ip',
   primary key (id)
);

alter table t_sys_task comment '系统任务处理表';