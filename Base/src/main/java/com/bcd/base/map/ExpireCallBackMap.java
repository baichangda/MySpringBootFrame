package com.bcd.base.map;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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
    public final static Logger logger = LoggerFactory.getLogger(ExpireCallBackMap.class);

    private final Map<String, Value<V>> dataMap = new ConcurrentHashMap<>();

    /**
     * 用于执行回调任务线程池
     */
    private ScheduledExecutorService expirePool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private ExecutorService callbackPool;

    /**
     * @param callbackPool 回调任务线程池
     */
    public ExpireCallBackMap(ExecutorService callbackPool) {
        this.callbackPool = callbackPool;
    }

    public static void main(String[] args) throws InterruptedException {
        logger.info("test");
        ExpireCallBackMap<String> map = new ExpireCallBackMap<>(Executors.newSingleThreadExecutor());
        map.put("1", Instant.now().toString(), 1, TimeUnit.SECONDS, (k, v) -> {
            logger.info("{}",v);
            logger.info("{}",Instant.now());
        });
        map.put("2", Instant.now().toString(), 10, TimeUnit.SECONDS, (k, v) -> {
            logger.info("{}",v);
            logger.info("{}",Instant.now());
        });
    }


    /**
     * @param k
     * @return
     */
    public V get(String k) {
        Value<V> expireValue;
        synchronized (k.intern()) {
            expireValue = dataMap.get(k);
        }
        return expireValue==null?null:expireValue.getVal();
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
        Value<V> old;
        synchronized (k.intern()) {
            old = dataMap.remove(k);
            if (old != null) {
                boolean res = old.getFuture().cancel(false);
                if (!res) {
                    logger.warn("cancel future failed in put for key[{}]", k);
                }
            }
            ScheduledFuture<?> future = expirePool.schedule(() -> {
                Value<V> expired;
                synchronized (k.intern()) {
                    expired = dataMap.remove(k);
                }
                //如果移除的是当前值、且回调不为null
                if (expired != null && expired.getVal() == v && callback != null) {
                    callbackPool.execute(() -> callback.accept(k, v));
                }

            }, expiredTime, unit);
            Value<V> expireValue = new Value<>(v, future);
            dataMap.put(k, expireValue);
        }
        return old==null?null:old.getVal();
    }

    /**
     * @param k
     * @return
     */
    public V remove(String k) {
        Value<V> removed;
        synchronized (k.intern()) {
            removed = dataMap.remove(k);
            if (removed != null) {
                boolean res = removed.getFuture().cancel(false);
                if (!res) {
                    logger.warn("cancel future failed in remove for key[{}]", k);
                }
            }
        }
        return removed==null?null:removed.getVal();
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
