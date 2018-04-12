package com.bcd.config.aliyun.properties.mns;

public class MnsTopicProperties {
    public String topic;
    public String queue;
    public String subscription;
    public boolean enable;
    public int queueWaitSeconds;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getQueueWaitSeconds() {
        return queueWaitSeconds;
    }

    public void setQueueWaitSeconds(int queueWaitSeconds) {
        this.queueWaitSeconds = queueWaitSeconds;
    }
}
