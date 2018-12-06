package com.bcd.base.map;


import java.util.function.BiConsumer;

public class ExpireValue<T> {
    private Long time;
    private Long aliveTime;
    private T val;
    private BiConsumer callback;

    public ExpireValue(Long time, Long aliveTime, T val) {
        this(time,aliveTime,val,null);
    }

    public ExpireValue(Long time, Long aliveTime, T val,BiConsumer callback) {
        this.time = time;
        this.aliveTime = aliveTime;
        this.val = val;
        this.callback=callback;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getAliveTime() {
        return aliveTime;
    }

    public void setAliveTime(Long aliveTime) {
        this.aliveTime = aliveTime;
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

    public boolean isExpired(){
        if(aliveTime<0){
            return false;
        }else {
            return System.currentTimeMillis() - time > aliveTime;
        }
    }
}
