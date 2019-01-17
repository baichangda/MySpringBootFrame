package com.bcd.sys;

import java.io.Serializable;

public interface UserDataAccess<K extends Serializable> {
    K getId();

    String getUsername();

    String getTimeZone();
}
