CREATE TABLE IF NOT EXISTS t_sys_menu
(
    id               bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id        bigint COMMENT '父菜单id',
    name             varchar(50)     NOT NULL COMMENT '菜单名称',
    url              varchar(256) COMMENT 'url地址',
    icon             varchar(256) COMMENT '图标',
    order_num        int             NOT NULL COMMENT '排序',
    create_time      timestamp       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id   bigint COMMENT '创建人id',
    create_user_name varchar(50) COMMENT '创建人姓名',
    update_time      timestamp       NULL COMMENT '更新时间',
    update_user_id   bigint COMMENT '更新人id',
    update_user_name varchar(50) COMMENT '更新人姓名',
    PRIMARY KEY (id)
);

ALTER TABLE t_sys_menu
    COMMENT '菜单表';