package com.bcd.base.support_jdbc.service;

import com.bcd.base.support_jdbc.bean.SuperBaseBean;

import java.io.Serializable;

/**
 * 获取用户id、名称, 由实体类继承并实现
 * 用于{@link BaseService#setCreateInfo(SuperBaseBean)}
 * 用于{@link BaseService#setCreateInfo(ParamPairs...)}
 * 用于{@link BaseService#setUpdateInfo(SuperBaseBean)}
 * 用于{@link BaseService#setUpdateInfo(ParamPairs...)}
 */
public interface UserInterface<K extends Serializable> {
    K getId();
    String getUserName();
}
