package com.bcd.base.map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 *
 * @param <K>
 * @param <V>
 */
public class MyCache<K,V> {

    Logger logger= LoggerFactory.getLogger(this.getClass());

    private final Map<K, ExpiredValue<K,V>> dataMap = new HashMap<>();

    /**
     * 1:强引用
     * 2:软引用
     * 3:弱引用
     */
    private int valueType=1;

    /**
     * 过期时间(毫秒)
     * -1:代表不过期
     * >=0:代表过期时间(毫秒)
     */
    private long expiredInMills =-1;

    private RemoveListener<K,V> expiredListener;

    private ExecutorService expiredListenerExecutor;


    /**
     *  引用队列移除
     */
    private ReferenceQueue<ReferenceData<K,V>> referenceQueue;

    private ExecutorService referenceQueueExecutor;

    private Future<?> referenceQueueFuture;


    /**
     * 扫描清除过期值
     */
    private ScheduledExecutorService clearExpiredValueExecutor;

    private ScheduledFuture<?> clearExpiredValueScheduledFuture;


    private volatile boolean stop=false;

    public synchronized MyCache<K,V> withSoftReferenceValue(){
            this.valueType = 2;
            initReferenceQueue();
            return this;
    }

    public synchronized MyCache<K,V> withWeakReferenceValue(){
            this.valueType = 3;
            initReferenceQueue();
            return this;
    }

    public synchronized MyCache<K,V> expiredAfter(long expired, TimeUnit unit){
            this.expiredInMills = unit.toMillis(expired);
            return this;
    }

    public synchronized MyCache<K,V> withClearExpiredValueExecutor(ScheduledExecutorService pool,
                                                      long initialDelay,
                                                      long period,
                                                      TimeUnit unit){
            this.clearExpiredValueExecutor = pool;
            this.clearExpiredValueScheduledFuture = pool.scheduleAtFixedRate(() -> {
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
            }, initialDelay, period, unit);
            return this;
    }

    public synchronized MyCache<K,V> withRemoveListener(RemoveListener<K,V> removeListener, ExecutorService removeListenerExecutor){
            this.expiredListener = Objects.requireNonNull(removeListener);
            this.expiredListenerExecutor = Objects.requireNonNull(removeListenerExecutor);
            return this;
    }

    private void initReferenceQueue(){
            if(referenceQueue==null){
                this.referenceQueue = new ReferenceQueue<>();
                this.referenceQueueExecutor = Executors.newSingleThreadExecutor();
                this.referenceQueueFuture = referenceQueueExecutor.submit(() -> {
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

    private void triggerExpiredListener(K k, V v){
        if(expiredListenerExecutor!=null&&expiredListener!=null){
            expiredListenerExecutor.execute(()->{
                expiredListener.onRemove(k,v);
            });
        }
    }

    public synchronized void destroy(){
            this.stop = true;
            this.clearExpiredValueExecutor.shutdown();
            this.referenceQueueExecutor.shutdown();
            this.referenceQueueFuture.cancel(true);
            this.clearExpiredValueScheduledFuture.cancel(false);
            this.clearExpiredValueExecutor = null;
            this.referenceQueueExecutor = null;
    }


    public synchronized V get(K k){
            ExpiredValue<K,V> expiredValue =dataMap.get(k);
            if(expiredValue==null){
                return null;
            }else{
                if (expiredValue.isExpired()) {
                    dataMap.remove(expiredValue.getKey());
                    triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    return null;
                }else{
                    return expiredValue.getValue();
                }
            }
    }

    public synchronized V put(K k,V v){
            ExpiredValue<K,V> expiredValue= ExpiredValue.newExpiredValue(k,v, expiredInMills, valueType,referenceQueue);
            ExpiredValue<K,V> oldExpiredValue=dataMap.put(k,expiredValue);
            if(oldExpiredValue==null){
                return null;
            }else{
                if(oldExpiredValue.isExpired()){
                    triggerExpiredListener(oldExpiredValue.getKey(),oldExpiredValue.getValue());
                    return null;
                }else{
                    return oldExpiredValue.getValue();
                }
            }
    }

    public synchronized V putIfAbsent(K k, V v) {
            ExpiredValue<K,V> oldExpiredValue = dataMap.get(k);
            if (oldExpiredValue == null) {
                ExpiredValue<K,V> expiredValue = ExpiredValue.newExpiredValue(k,v, expiredInMills, valueType,referenceQueue);
                dataMap.put(k, expiredValue);
                return null;
            }else{
                if(oldExpiredValue.isExpired()){
                    ExpiredValue<K,V> expiredValue = ExpiredValue.newExpiredValue(k,v, expiredInMills, valueType,referenceQueue);
                    dataMap.put(k, expiredValue);
                    triggerExpiredListener(oldExpiredValue.getKey(),oldExpiredValue.getValue());
                    return null;
                }else {
                    return oldExpiredValue.getValue();
                }
            }
    }

    public synchronized V computeIfAbsent(K k, Function<K,V> mappingFunction) {
            ExpiredValue<K,V> oldExpiredValue = dataMap.get(k);
            if (oldExpiredValue == null) {
                V v = mappingFunction.apply(k);
                ExpiredValue<K,V> expiredValue = ExpiredValue.newExpiredValue(k,v, expiredInMills, valueType,referenceQueue);
                dataMap.put(k, expiredValue);
                return v;
            }else{
                if(oldExpiredValue.isExpired()){
                    V v = mappingFunction.apply(k);
                    ExpiredValue<K,V> expiredValue = ExpiredValue.newExpiredValue(k,v, expiredInMills, valueType,referenceQueue);
                    dataMap.put(k, expiredValue);
                    triggerExpiredListener(oldExpiredValue.getKey(),oldExpiredValue.getValue());
                    return v;
                }else {
                    return oldExpiredValue.getValue();
                }
            }
    }

    public synchronized V remove(K k) {
            ExpiredValue<K,V> expiredValue = dataMap.remove(k);
            if(expiredValue==null){
                return null;
            }else{
                if(expiredValue.isExpired()){
                    triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    return null;
                }else{
                    return expiredValue.getValue();
                }
            }
    }

    public synchronized void clear() {
            this.dataMap.clear();
    }

    public synchronized boolean contains(K k){
            ExpiredValue<K,V> expiredValue = dataMap.get(k);
            if(expiredValue==null){
                return false;
            }else{
                if(expiredValue.isExpired()){
                    dataMap.remove(k);
                    triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    return false;
                }else{
                    return true;
                }
            }
    }

    public synchronized int size(){
            int count=0;
            Iterator<Map.Entry<K, ExpiredValue<K,V>>> iterator = dataMap.entrySet().iterator();
            while(iterator.hasNext()){
                ExpiredValue<K,V> expiredValue = iterator.next().getValue();
                if(expiredValue!=null){
                    if(expiredValue.isExpired()){
                        iterator.remove();
                        triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    }else{
                        count++;
                    }
                }
            }
            return count;
    }

    public synchronized Set<K> keySet(){
            Set<K> set=new HashSet<>();
            Iterator<Map.Entry<K, ExpiredValue<K,V>>> iterator = dataMap.entrySet().iterator();
            while(iterator.hasNext()){
                ExpiredValue<K,V> expiredValue = iterator.next().getValue();
                if(expiredValue!=null){
                    if(expiredValue.isExpired()){
                        iterator.remove();
                        triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    }else{
                        set.add(expiredValue.getKey());
                    }
                }
            }
            return set;
    }

    public synchronized Collection<V> values(){
            Collection<V> collection=new ArrayList<>();
            Iterator<Map.Entry<K, ExpiredValue<K,V>>> iterator = dataMap.entrySet().iterator();
            while(iterator.hasNext()){
                ExpiredValue<K,V> expiredValue = iterator.next().getValue();
                if(expiredValue!=null){
                    if(expiredValue.isExpired()){
                        iterator.remove();
                        triggerExpiredListener(expiredValue.getKey(),expiredValue.getValue());
                    }else{
                        collection.add(expiredValue.getValue());
                    }
                }
            }
            return collection;
    }


}
