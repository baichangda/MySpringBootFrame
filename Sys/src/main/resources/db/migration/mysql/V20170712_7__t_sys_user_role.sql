CREATE TABLE IF NOT EXISTS t_sys_user_role
(
    user_id   bigint COMMENT '关联用户id',
    role_code varchar(50) COMMENT '关联角色编码',
    PRIMARY KEY (user_id, role_code)
);

ALTER TABLE t_sys_user_role
    COMMENT '用户与角色关联关系表';