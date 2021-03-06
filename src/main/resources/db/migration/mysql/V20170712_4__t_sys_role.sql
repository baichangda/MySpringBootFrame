CREATE TABLE IF NOT EXISTS t_sys_role
(
    id               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name             varchar(20)     NOT NULL COMMENT '角色名称',
    code             varchar(50)     NOT NULL COMMENT '编码',
    remark           varchar(256) COMMENT '备注',
    create_time      timestamp       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id   bigint COMMENT '创建人id',
    create_user_name varchar(50) COMMENT '创建人姓名',
    update_time      timestamp       NULL COMMENT '更新时间',
    update_user_id   bigint COMMENT '更新人id',
    update_user_name varchar(50) COMMENT '更新人姓名',
    PRIMARY KEY (id)
);

ALTER TABLE t_sys_role
    COMMENT '角色表';