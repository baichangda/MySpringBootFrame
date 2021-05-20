package com.bcd.base.map;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * 可以插入过期key-value的 Map
 * 插入的key-value会在expireTime后被移除
 * 过期策略:
 * 1、定时线程池执行 计划移除任务
 * 在过期被移除后,会调用设置的过期回调方法
 * <p>
 * 适用于绑定过期回调
 *
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireCallBackMap<V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireCallBackMap.class);
    private final Map<String, Value<V>> dataMap = new HashMap<>();

    /**
     * 用于执行回调任务线程池
     */
    private ScheduledExecutorService expirePool = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService callbackPool;

    /**
     * @param callbackPool 回调任务线程池
     */
    public ExpireCallBackMap(ExecutorService callbackPool) {
        this.callbackPool = callbackPool;
    }

    public static void main(String[] args) throws InterruptedException {
        ExpireCallBackMap<String> map = new ExpireCallBackMap<>(Executors.newSingleThreadExecutor());
        map.put("1", Instant.now().toString(), 1, TimeUnit.SECONDS, (k, v) -> {
            System.out.println(v);
            System.out.println(Instant.now());
        });
        map.put("2", Instant.now().toString(), 10, TimeUnit.SECONDS, (k, v) -> {
            System.out.println(v);
            System.out.println(Instant.now());
        });
    }


    private V getVal(Value<V> value, boolean cancelFuture) {
        if (value == null) {
            return null;
        } else {
            if (cancelFuture && value.getFuture() != null) {
                value.getFuture().cancel(false);
            }
            return value.getVal();
        }
    }


    /**
     * @param k
     * @return
     */
    public V get(String k) {
        synchronized (k.intern()) {
            Value<V> expireValue = dataMap.get(k);
            return getVal(expireValue, false);
        }
    }

    /**
     * @param k
     * @param v
     * @param expiredTime
     * @return
     */
    public V put(String k, V v, long expiredTime, TimeUnit unit) {
        return put(k, v, expiredTime, unit, null);
    }

    /**
     * @param k
     * @param v
     * @param expiredTime
     * @param callback
     * @return
     */
    public V put(String k, V v, long expiredTime, TimeUnit unit, BiConsumer<String, V> callback) {
        ScheduledFuture<?> future = expirePool.schedule(() -> {
            V old = remove(k);
            //如果移除的是当前值、且回调不为null
            if (old == v && callback != null) {
                callbackPool.execute(() ->
                        callback.accept(k, v)
                );
            }
        }, expiredTime, unit);
        Value<V> expireValue = new Value<>(v, future);
        synchronized (k.intern()) {
            Value<V> oldVal = dataMap.put(k, expireValue);
            return getVal(oldVal, true);
        }
    }

    /**
     * @param k
     * @return
     */
    public V remove(String k) {
        synchronized (k.intern()) {
            Value<V> oldVal = dataMap.remove(k);
            return getVal(oldVal, true);
        }
    }

    /**
     * @return
     */
    public void removeAll() {
        for (String k : dataMap.keySet()) {
            remove(k);
        }
    }

    @AllArgsConstructor
    @Getter
    public final static class Value<T> {
        private T val;
        //可能为null
        private Future future;
    }

}
