create table if not exists t_sys_user_role
(
    user_id   bigint comment '关联用户id',
    role_code varchar(50) comment '关联角色编码',
    primary key (user_id, role_code)
);

alter table t_sys_user_role
    comment '用户与角色关联关系表';