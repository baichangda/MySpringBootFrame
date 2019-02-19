package com.bcd.sys.data.bean;

import java.io.Serializable;

public interface CommonUserBean<K extends Serializable> {
    K getId();

    String getUsername();

    String getTimeZone();
}
