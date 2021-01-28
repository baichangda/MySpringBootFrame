package com.bcd.base.map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * 本地自定义缓存、所有的方法均使用 synchronized 修饰、为线程安全
 * <p>
 * 支持如下功能:
 * 1、缓存失效时间设置 {@link #expiredAfter(long, TimeUnit)}
 * <p>
 * 2、缓存值类型设置
 * {@link #withSoftReferenceValue()}、
 * {@link #withWeakReferenceValue()}
 * 如果设置缓存值、则会默认启动一个线程来监测当垃圾回收器回收时候触发缓存值删除
 * <p>
 * 3、定时扫描清除过期缓存
 * {@link #withClearExpiredValueExecutor(ScheduledExecutorService, long, long, TimeUnit)}
 * <p>
 * 4、注册过期移除监听器
 * {@link #withRemoveListener(RemoveListener, ExecutorService)}
 * <p>
 * 如下方法在主动调用时候会触发检查过期缓存并回调
 * {@link #get(K)}
 * {@link #put(K, V)}
 * {@link #putIfAbsent(K, V)}
 * {@link #computeIfAbsent(K, Function)}
 * {@link #remove(K)}
 * {@link #contains(K)}
 * {@link #size()}
 * {@link #keySet()}
 * {@link #values()}
 *
 * @param <K>
 * @param <V>
 */
public class MyCache<K, V> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<K, ExpiredValue<K, V>> dataMap = new HashMap<>();

    /**
     * 1:强引用
     * 2:软引用
     * 3:弱引用
     */
    private int valueType = 1;

    /**
     * 过期时间(毫秒)
     * -1:代表不过期
     * >=0:代表过期时间(毫秒)
     */
    private long expiredInMillis = -1;

    /**
     * 过期值移除触发回调
     */
    private RemoveListener<K, V> expiredListener;
    private ExecutorService expiredListener_executor;


    /**
     * 当垃圾回收器回收时候触发缓存值删除
     */
    private ReferenceQueue<ReferenceData<K, V>> referenceQueue;
    private ExecutorService referenceQueue_executor;
    private Future<?> referenceQueue_future;
    private boolean stop = false;


    /**
     * 定时扫描清除过期缓存
     */
    private ScheduledExecutorService clearExpiredValue_executor;
    private long clearExpiredValue_executor_initialDelay;
    private long clearExpiredValue_executor_period;
    private TimeUnit clearExpiredValue_executor_unit;
    private ScheduledFuture<?> clearExpiredValueExecutor_scheduledFuture;


    public synchronized MyCache<K, V> init() {
        initReferenceQueue();
        initClearExpiredValue();
        return this;
    }

    public synchronized MyCache<K, V> withSoftReferenceValue() {
        this.valueType = 2;
        return this;
    }

    public synchronized MyCache<K, V> withWeakReferenceValue() {
        this.valueType = 3;
        return this;
    }

    public synchronized MyCache<K, V> expiredAfter(long expired, TimeUnit unit) {
        this.expiredInMillis = unit.toMillis(expired);
        return this;
    }

    public synchronized MyCache<K, V> withClearExpiredValueExecutor(ScheduledExecutorService executor,
                                                                    long initialDelay,
                                                                    long period,
                                                                    TimeUnit unit) {
        this.clearExpiredValue_executor = Objects.requireNonNull(executor);
        this.clearExpiredValue_executor_initialDelay = initialDelay;
        this.clearExpiredValue_executor_period = period;
        this.clearExpiredValue_executor_unit = unit;
        return this;
    }

    public synchronized MyCache<K, V> withRemoveListener(RemoveListener<K, V> removeListener, ExecutorService removeListenerExecutor) {
        this.expiredListener = Objects.requireNonNull(removeListener);
        this.expiredListener_executor = Objects.requireNonNull(removeListenerExecutor);
        return this;
    }


    private void initReferenceQueue() {
        if (this.valueType == 2 || this.valueType == 3) {
            this.referenceQueue = new ReferenceQueue<>();
            this.referenceQueue_executor = Executors.newSingleThreadExecutor();
            this.referenceQueue_future = referenceQueue_executor.submit(() -> {
                try {
                    while (!stop) {
                        Reference<? extends ReferenceData<K, V>> remove = referenceQueue.remove();
                        remove.clear();
                    }
                } catch (InterruptedException e) {
                    logger.info("thread interrupted");
                }
            });
        }
    }

    private void initClearExpiredValue() {
        this.clearExpiredValueExecutor_scheduledFuture = this.clearExpiredValue_executor.scheduleAtFixedRate(() -> {
                    synchronized (dataMap) {
                        Iterator<Map.Entry<K, ExpiredValue<K, V>>> iterator = dataMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            ExpiredValue<K, V> expireValue = iterator.next().getValue();
                            if (expireValue != null && expireValue.isExpired()) {
                                iterator.remove();
                                triggerExpiredListener(expireValue.getKey(), expireValue.getValue());
                            }
                        }
                    }
                }, this.clearExpiredValue_executor_initialDelay,
                this.clearExpiredValue_executor_period,
                this.clearExpiredValue_executor_unit);
    }

    private void triggerExpiredListener(K k, V v) {
        if (expiredListener_executor != null && expiredListener != null) {
            expiredListener_executor.execute(() -> {
                expiredListener.onRemove(k, v);
            });
        }
    }

    public synchronized void destroy() {
        this.stop = true;
        this.clearExpiredValue_executor.shutdown();
        this.referenceQueue_executor.shutdown();

        this.referenceQueue_future.cancel(true);
        this.clearExpiredValueExecutor_scheduledFuture.cancel(false);

        this.clearExpiredValue_executor = null;
        this.referenceQueue_executor = null;
    }


    public synchronized V get(K k) {
        ExpiredValue<K, V> expiredValue = dataMap.get(k);
        if (expiredValue == null) {
            return null;
        } else {
            if (expiredValue.isExpired()) {
                dataMap.remove(expiredValue.getKey());
                triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                return null;
            } else {
                return expiredValue.getValue();
            }
        }
    }

    public synchronized V put(K k, V v) {
        ExpiredValue<K, V> expiredValue = ExpiredValue.newExpiredValue(k, v, expiredInMillis, valueType, referenceQueue);
        ExpiredValue<K, V> oldExpiredValue = dataMap.put(k, expiredValue);
        if (oldExpiredValue == null) {
            return null;
        } else {
            if (oldExpiredValue.isExpired()) {
                triggerExpiredListener(oldExpiredValue.getKey(), oldExpiredValue.getValue());
                return null;
            } else {
                return oldExpiredValue.getValue();
            }
        }
    }

    public synchronized V putIfAbsent(K k, V v) {
        ExpiredValue<K, V> oldExpiredValue = dataMap.get(k);
        if (oldExpiredValue == null) {
            ExpiredValue<K, V> expiredValue = ExpiredValue.newExpiredValue(k, v, expiredInMillis, valueType, referenceQueue);
            dataMap.put(k, expiredValue);
            return null;
        } else {
            if (oldExpiredValue.isExpired()) {
                ExpiredValue<K, V> expiredValue = ExpiredValue.newExpiredValue(k, v, expiredInMillis, valueType, referenceQueue);
                dataMap.put(k, expiredValue);
                triggerExpiredListener(oldExpiredValue.getKey(), oldExpiredValue.getValue());
                return null;
            } else {
                return oldExpiredValue.getValue();
            }
        }
    }

    public synchronized V computeIfAbsent(K k, Function<K, V> mappingFunction) {
        ExpiredValue<K, V> oldExpiredValue = dataMap.get(k);
        if (oldExpiredValue == null) {
            V v = mappingFunction.apply(k);
            ExpiredValue<K, V> expiredValue = ExpiredValue.newExpiredValue(k, v, expiredInMillis, valueType, referenceQueue);
            dataMap.put(k, expiredValue);
            return v;
        } else {
            if (oldExpiredValue.isExpired()) {
                V v = mappingFunction.apply(k);
                ExpiredValue<K, V> expiredValue = ExpiredValue.newExpiredValue(k, v, expiredInMillis, valueType, referenceQueue);
                dataMap.put(k, expiredValue);
                triggerExpiredListener(oldExpiredValue.getKey(), oldExpiredValue.getValue());
                return v;
            } else {
                return oldExpiredValue.getValue();
            }
        }
    }

    public synchronized V remove(K k) {
        ExpiredValue<K, V> expiredValue = dataMap.remove(k);
        if (expiredValue == null) {
            return null;
        } else {
            if (expiredValue.isExpired()) {
                triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                return null;
            } else {
                return expiredValue.getValue();
            }
        }
    }

    public synchronized void clear() {
        this.dataMap.clear();
    }

    public synchronized boolean contains(K k) {
        ExpiredValue<K, V> expiredValue = dataMap.get(k);
        if (expiredValue == null) {
            return false;
        } else {
            if (expiredValue.isExpired()) {
                dataMap.remove(k);
                triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                return false;
            } else {
                return true;
            }
        }
    }

    public synchronized int size() {
        int count = 0;
        Iterator<Map.Entry<K, ExpiredValue<K, V>>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            ExpiredValue<K, V> expiredValue = iterator.next().getValue();
            if (expiredValue != null) {
                if (expiredValue.isExpired()) {
                    iterator.remove();
                    triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized Set<K> keySet() {
        Set<K> set = new HashSet<>();
        Iterator<Map.Entry<K, ExpiredValue<K, V>>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            ExpiredValue<K, V> expiredValue = iterator.next().getValue();
            if (expiredValue != null) {
                if (expiredValue.isExpired()) {
                    iterator.remove();
                    triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                } else {
                    set.add(expiredValue.getKey());
                }
            }
        }
        return set;
    }

    public synchronized Collection<V> values() {
        Collection<V> collection = new ArrayList<>();
        Iterator<Map.Entry<K, ExpiredValue<K, V>>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            ExpiredValue<K, V> expiredValue = iterator.next().getValue();
            if (expiredValue != null) {
                if (expiredValue.isExpired()) {
                    iterator.remove();
                    triggerExpiredListener(expiredValue.getKey(), expiredValue.getValue());
                } else {
                    collection.add(expiredValue.getValue());
                }
            }
        }
        return collection;
    }


}
