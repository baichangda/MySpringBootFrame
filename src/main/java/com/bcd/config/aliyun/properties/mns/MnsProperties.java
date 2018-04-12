package com.bcd.config.aliyun.properties.mns;

public class MnsProperties {
    public String endPoint;

    public MnsTopicProperties test1;

    public MnsTopicProperties test2;

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public MnsTopicProperties getTest1() {
        return test1;
    }

    public void setTest1(MnsTopicProperties test1) {
        this.test1 = test1;
    }

    public MnsTopicProperties getTest2() {
        return test2;
    }

    public void setTest2(MnsTopicProperties test2) {
        this.test2 = test2;
    }
}
