CREATE TABLE IF NOT EXISTS t_log
(
   id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志id',
   oper_type             INT(2) COMMENT '日志类型： 1-新增 2-修改 3-删除',
   resource_id          BIGINT UNSIGNED,
   resource_class       varchar(100),
   create_time          TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
   create_user_id       BIGINT UNSIGNED COMMENT '操作人ID',
   PRIMARY KEY (id)
);

ALTER TABLE t_log COMMENT '日志表';