package com.bcd.base.support_jdbc.bean;


import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
public interface SuperBaseBean<K extends Serializable> extends Serializable {
    K getId();
    void setId(K id);
}
