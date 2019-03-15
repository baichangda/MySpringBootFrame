package com.bcd.base.map;


import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 可以插入过期key-value的 ConcurrentHashMap
 * 插入的key-value会在aliveTime后被移除,如果aliveTime为-1则表明不过期
 * 过期策略:
 * 1、懒汉模式: 在调用get时候检查,如果过期则移除
 * 2、定期检查模式: 启动计划任务执行器周期性的检查所有此类的实例,检查并移除里面过期的key
 * <p>
 * 在过期被移除后,会调用设置的过期回调方法
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireConcurrentMap<K, V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireConcurrentMap.class);
    private final Map<K, ExpireValue<V>> dataMap = new ConcurrentHashMap<>();
    private final ExpireKeyLinkedList expireKeyList = new ExpireKeyLinkedList();
    private final ReentrantReadWriteLock lock=new ReentrantReadWriteLock();

    /**
     * 用于从map中检索出过期key并移除 定时任务线程池
     */
    private ScheduledExecutorService expireScanPool;
    private long initDelay;
    private long delay;

    /**
     * 用于执行过期的回调方法线程池
     */
    private ExecutorService expireWorkPool;

    private final static ExecutorService DEFAULT_EXPIRE_WORK_POOL=Executors.newCachedThreadPool();

    private void startExpireSchedule(){
        expireScanPool.scheduleWithFixedDelay(() -> {
            lock.writeLock().lock();
            try {
                List<ExpireKey<K,V>> keyList = expireKeyList.removeExpired(System.currentTimeMillis());
                keyList.forEach(key -> {
                    ExpireValue expireValue = dataMap.remove(key.getKey());
                    callback(key.getKey(), expireValue);
                });
            } finally {
                lock.writeLock().unlock();
            }
        }, initDelay, delay, TimeUnit.MILLISECONDS);
    }

    public ExpireConcurrentMap() {
        this(Executors.newScheduledThreadPool(1),1000L,2000L,DEFAULT_EXPIRE_WORK_POOL);
    }

    public ExpireConcurrentMap(ScheduledExecutorService expireScanPool,long initDelay,long delay,ExecutorService expireWorkPool){
        this.expireScanPool=expireScanPool;
        this.initDelay=initDelay;
        this.delay=delay;
        this.expireWorkPool=expireWorkPool;
        startExpireSchedule();
    }



    private void callback(K k, ExpireValue<V> expireValue) {
        if (expireValue.getCallback()!= null) {
            expireWorkPool.execute(() -> {
                try {
                    expireValue.getCallback().accept(k, expireValue.getVal());
                } catch (Exception e) {
                    ExceptionUtil.printException(e);
                }
            });
        }
    }


    public V get(K k) {
        ExpireValue<V> expireValue;
        lock.readLock().lock();
        try {
            expireValue = dataMap.get(k);
        }finally {
            lock.readLock().unlock();
        }
        if (expireValue == null) {
            return null;
        } else {
            if (expireValue.isExpired()) {
                remove(k);
                callback(k,expireValue);
                return null;
            } else {
                return expireValue.getVal();
            }
        }
    }

    public V put(K k, V v, long aliveTime) {
        return put(k, v, aliveTime, null);
    }

    public V put(K k, V v, long aliveTime, BiConsumer<K, V> callback) {
        lock.writeLock().lock();
        try {
            ExpireValue<V> expireValue = new ExpireValue<>(System.currentTimeMillis() + aliveTime, v, callback);
            ExpireValue<V> val = dataMap.put(k, expireValue);
            ExpireKey<K, V> expireKey = new ExpireKey<>(k, expireValue);
            expireKeyList.add(expireKey);
            return val == null ? null : val.getVal();
        }finally {
            lock.writeLock().unlock();
        }
    }

    public V putIfAbsent(K k, V v, long aliveTime) {
        return put(k, v, aliveTime, null);
    }

    public V putIfAbsent(K k, V v, long aliveTime, BiConsumer<K, V> callback) {
        lock.writeLock().lock();
        try {
            ExpireValue<V> expireValue = new ExpireValue<>(System.currentTimeMillis() + aliveTime, v, callback);
            ExpireValue<V> val = dataMap.putIfAbsent(k, expireValue);
            ExpireKey<K, V> expireKey = new ExpireKey<>(k, expireValue);
            expireKeyList.add(expireKey);
            return val == null ? null : val.getVal();
        }finally {
            lock.writeLock().unlock();
        }
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime) {
        ExpireValue<V> val = dataMap.computeIfAbsent(k, e -> new ExpireValue<>(System.currentTimeMillis() + aliveTime, mappingFunction.apply(e)));
        return val == null ? null : val.getVal();
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime, BiConsumer<K, V> callback) {
        ExpireValue<V> val = dataMap.computeIfAbsent(k, e -> {
            lock.writeLock().lock();
            try {
                ExpireValue<V> expireValue = new ExpireValue<>(System.currentTimeMillis() + aliveTime, mappingFunction.apply(e), callback);
                ExpireKey<K, V> expireKey = new ExpireKey<>(k, expireValue);
                expireKeyList.add(expireKey);
                return expireValue;
            }finally {
                lock.writeLock().unlock();
            }
        });
        return val == null ? null : val.getVal();
    }

    public V remove(K k) {
        lock.writeLock().lock();
        try {
            ExpireValue<V> val = dataMap.remove(k);
            if (val != null) {
                val.setRemoved(true);
            }
            return val == null ? null : val.getVal();
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            this.dataMap.clear();
            this.expireKeyList.clear();
        }finally {
            lock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExpireConcurrentMap<String,String> map=new ExpireConcurrentMap<>();
        map.put("test1","test1",2000L,(k,v)->{
            System.out.println(v+"已经过期了");
        });
        map.put("test2","test2",5000L,(k,v)->{
            System.out.println(v+"已经过期了");
        });
    }

}

class ExpireKeyLinkedList<K,V> {
    transient Node first;

    AtomicInteger size=new AtomicInteger(0);

    public ExpireKeyLinkedList() {
    }

    public void add(ExpireKey<K,V> e) {
        if(first==null){
            first=new Node<>(null,e,null);
        }else{
            Node<ExpireKey> prev=null;
            Node<ExpireKey> next=first;
            while(true){
                Long t1=e.getExpireValue().getExpireTime();
                Long t2=next.item.getExpireValue().getExpireTime();
                if(t1>t2){
                    prev=next;
                    next=prev.next;
                    if(next==null){
                        prev.next=new Node<>(prev,e,null);
                        break;
                    }
                }else{
                    Node<ExpireKey> cur=new Node<>(prev,e,next);
                    if(prev==null){
                        first=cur;
                    }else{
                        prev.next = cur;
                    }
                    next.prev = cur;
                    break;
                }
            }
        }
        size.incrementAndGet();
    }

    public List<ExpireKey<K,V>> removeExpired(long ts) {
        List<ExpireKey<K,V>> resList = new ArrayList<>();
        if (first == null) {
            return resList;
        } else {
            Node<ExpireKey<K,V>> cur = first;
            int sum=0;
            while (cur != null) {
                boolean expired = cur.item.getExpireValue().getExpireTime()<=ts;
                if (expired) {
                    sum++;
                    if (!cur.item.getExpireValue().isRemoved()) {
                        resList.add(cur.item);
                    }
                    cur = cur.next;
                } else {
                    break;
                }
            }
            if(cur!=null){
                cur.prev=null;
            }
            first=cur;
            size.getAndAdd(-sum);
            return resList;
        }
    }

    public void clear(){
        first=null;
        size.set(0);
    }

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
