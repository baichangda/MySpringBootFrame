package com.bcd.config.aliyun.properties;

import com.bcd.config.aliyun.properties.mns.MnsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ConfigurationProperties(prefix = "aliyun")
public class AliyunProperties {
    public String secretKey;
    public String accessKey;
    public RocketMqProperties rocketMq;
    public TableStoreProperties tableStore;
    public MnsProperties mns;
    public SmsProperties sms;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public RocketMqProperties getRocketMq() {
        return rocketMq;
    }

    public void setRocketMq(RocketMqProperties rocketMq) {
        this.rocketMq = rocketMq;
    }

    public TableStoreProperties getTableStore() {
        return tableStore;
    }

    public void setTableStore(TableStoreProperties tableStore) {
        this.tableStore = tableStore;
    }

    public MnsProperties getMns() {
        return mns;
    }

    public void setMns(MnsProperties mns) {
        this.mns = mns;
    }

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(SmsProperties sms) {
        this.sms = sms;
    }
}

