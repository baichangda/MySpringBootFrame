package com.bcd.base.support_redis.register;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "provider")
public class ProviderProp {

    public String host;
    /**
     * 服务类别
     * 同一类服务提供者、应该是相同类型
     * 可以是多个,分割
     */
    public String types;

    /**
     * 上报周期
     */
    public Duration reportPeriod = Duration.ofSeconds(15);
    /**
     * 最大过期时间
     */
    public Duration expired = Duration.ofSeconds(30);

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public Duration getExpired() {
        return expired;
    }

    public void setExpired(Duration expired) {
        this.expired = expired;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Duration getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(Duration reportPeriod) {
        this.reportPeriod = reportPeriod;
    }
}
