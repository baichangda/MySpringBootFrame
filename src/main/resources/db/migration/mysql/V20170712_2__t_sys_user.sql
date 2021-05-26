CREATE TABLE IF NOT EXISTS t_sys_user
(
    id               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username         varchar(50)     NOT NULL COMMENT '用户名',
    password         varchar(100)    NOT NULL COMMENT '密码',
    email            varchar(100) COMMENT '邮箱',
    phone            varchar(11) COMMENT '手机号',
    real_name        varchar(50) COMMENT '真实姓名',
    sex              varchar(5) COMMENT '性别',
    birthday         timestamp       NULL COMMENT '生日',
    card_number      varchar(20) COMMENT '身份证号',
    status           int             NOT NULL DEFAULT 1 COMMENT '是否可用(0:禁用,1:可用)',
    create_time      timestamp       NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id   bigint COMMENT '创建人id',
    create_user_name varchar(50) COMMENT '创建人姓名',
    update_time      timestamp       NULL COMMENT '更新时间',
    update_user_id   bigint COMMENT '更新人id',
    update_user_name varchar(50) COMMENT '更新人姓名',
    create_ip        varchar(50) COMMENT '创建ip地址',
    update_ip        varchar(50) COMMENT '更新ip地址',
    PRIMARY KEY (id)
);

ALTER TABLE t_sys_user
    COMMENT '用户基础信息表';