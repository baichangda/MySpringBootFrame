package com.bcd.base.support_redis.register;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "register")
public class RegisterProp {

    /**
     * ip:端口
     */
    public String host;
    /**
     * 服务类别
     * 同一类服务提供者、应该是相同类型
     * 可以是多个,分割
     */
    public String types;

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
