package com.bcd.base.support_notify;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "notify")
public class NotifyProp {
    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
