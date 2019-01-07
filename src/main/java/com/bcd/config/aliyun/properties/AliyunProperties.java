package com.bcd.config.aliyun.properties;

import com.bcd.config.aliyun.properties.mns.MnsProperties;

//@Configuration
//@ConfigurationProperties(prefix = "aliyun")
public class AliyunProperties {
    public String accessKeyId;
    public String accessKeySecret;
    public RocketMqProperties rocketMq;
    public MnsProperties mns;
    public SmsProperties sms;
    public OSSProperties oss;
    public LiveProperties live;

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public RocketMqProperties getRocketMq() {
        return rocketMq;
    }

    public void setRocketMq(RocketMqProperties rocketMq) {
        this.rocketMq = rocketMq;
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

    public OSSProperties getOss() {
        return oss;
    }

    public void setOss(OSSProperties oss) {
        this.oss = oss;
    }

    public LiveProperties getLive() {
        return live;
    }

    public void setLive(LiveProperties live) {
        this.live = live;
    }
}

