package com.bcd.base.map;


import java.util.function.BiConsumer;

public class ExpireValue<T> {
    private Long expireTime;
    private T val;
    private BiConsumer callback;
    private boolean removed;

    public ExpireValue(Long expireTime, T val) {
        this(expireTime, val, null);
    }

    public ExpireValue(Long expireTime, T val, BiConsumer callback) {
        this.expireTime = expireTime;
        this.val = val;
        this.callback = callback;
        this.removed = false;
    }


    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }

    public BiConsumer getCallback() {
        return callback;
    }

    public void setCallback(BiConsumer callback) {
        this.callback = callback;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
