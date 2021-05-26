CREATE TABLE IF NOT EXISTS t_sys_permission
(
    id               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    name             varchar(20)     NOT NULL COMMENT '角色名称',
    code             varchar(50)     NOT NULL COMMENT '编码',
    remark           varchar(256) COMMENT '备注',
    create_time      timestamp       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id   bigint COMMENT '创建人id',
    create_user_name varchar(50) COMMENT '创建人姓名',
    update_time      timestamp       NULL COMMENT '更新时间',
    update_user_id   bigint COMMENT '更新人id',
    update_user_name varchar(50) COMMENT '更新人姓名',
    create_ip        varchar(50) COMMENT '创建ip地址',
    update_ip        varchar(50) COMMENT '更新ip地址',
    PRIMARY KEY (id)
);

ALTER TABLE t_sys_permission
    COMMENT '角色与权限关系表';