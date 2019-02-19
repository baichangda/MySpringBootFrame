package com.bcd.sys;

import java.io.Serializable;

public interface UserData<K extends Serializable> {
    K getId();

    String getUsername();

    String getTimeZone();
}
