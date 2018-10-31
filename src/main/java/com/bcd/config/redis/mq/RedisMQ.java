package com.bcd.base.redis.mq;

import java.util.List;

public interface RedisMQ<V> {
    /**
     * 发送信息到mq
     * @param data
     */
    void send(Object data);

    /**
     * 批量发送信息到mq
     * @param dataList
     */
    void sendBatch(List dataList);

    /**
     * 当有消息时候回调方法
     * @return
     */
    void onMessage(V data);

    /**
     * 取消监听
     */
    void watch();

    /**
     * 取消监听
     */
    void unWatch();
}
