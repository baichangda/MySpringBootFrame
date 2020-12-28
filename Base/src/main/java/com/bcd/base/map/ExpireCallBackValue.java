package com.bcd.base.map;


import java.util.function.BiConsumer;

public class ExpireCallBackValue<T> {
    private Long expireTime;
    private T val;
    private BiConsumer callback;

    public ExpireCallBackValue(Long expireTime, T val, BiConsumer callback) {
        this.expireTime = expireTime;
        this.val = val;
        this.callback = callback;
    }

    public T getVal() {
        return val;
    }

    public BiConsumer getCallback() {
        return callback;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }
}
