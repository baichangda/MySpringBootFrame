package com.bcd.base.map;


import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 可以插入过期key-value的 Map
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
public class ExpireThreadSafeMap<K, V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireThreadSafeMap.class);
    private final Map<K, ExpireValue<V>> dataMap = new HashMap<>();
    private final ExpireKeyLinkedList expireKeyList = new ExpireKeyLinkedList();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 用于从map中检索出过期key并移除 定时任务线程池
     */
    private ScheduledExecutorService expireScanPool;
    private Long delay;

    /**
     * 用于执行过期的回调方法线程池
     * 为null则不触发回调
     */
    private ExecutorService expireWorkPool;

    private void startExpireSchedule() {
        expireScanPool.scheduleWithFixedDelay(() -> {
            lock.writeLock().lock();
            try {
                List<ExpireKey<K, V>> keyList = expireKeyList.removeExpired(System.currentTimeMillis());
                keyList.forEach(key -> {
                    ExpireValue expireValue = dataMap.remove(key.getKey());
                    if (expireValue != null) {
                        callback(key.getKey(), expireValue);
                    }
                });
            } finally {
                lock.writeLock().unlock();
            }
        }, 3000, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 不开启扫描和回调
     */
    public ExpireThreadSafeMap(){
        this(null,null);
    }

    /**
     * @param delay 扫描计划执行间隔,为null表示不开启扫描
     */
    public ExpireThreadSafeMap(Long delay) {
        this(delay,null);
    }

    /**
     * @param delay          扫描计划执行间隔,为null则表示不进行扫描
     * @param expireWorkPool 过期回调执行线程池 传入null代表不触发回调
     */
    public ExpireThreadSafeMap(Long delay,ExecutorService expireWorkPool) {
        this.delay = delay;
        this.expireWorkPool = expireWorkPool;
    }

    public void init(){
        if(delay!=null){
            expireScanPool=Executors.newSingleThreadScheduledExecutor();
            startExpireSchedule();
        }
    }

    public void destroy(){
        if(expireScanPool!=null){
            expireScanPool.shutdown();
        }
    }


    private void callback(K k, ExpireValue<V> expireValue) {
        if(expireWorkPool!=null) {
            if (expireValue.getCallback() != null) {
                expireWorkPool.execute(() -> {
                    try {
                        expireValue.getCallback().accept(k, expireValue.getVal());
                    } catch (Exception e) {
                        ExceptionUtil.printException(e);
                    }
                });
            }
        }
    }


    public V get(K k) {
        ExpireValue<V> expireValue;
        lock.readLock().lock();
        try {
            expireValue = dataMap.get(k);
        } finally {
            lock.readLock().unlock();
        }
        if (expireValue == null) {
            return null;
        } else {
            if (expireValue.isExpired()) {
                V v = remove(k);
                if (v != null) {
                    callback(k, expireValue);
                }
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
        } finally {
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime) {
        return computeIfAbsent(k, mappingFunction, aliveTime, null);
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime, BiConsumer<K, V> callback) {
        lock.writeLock().lock();
        try {
            V v = get(k);
            if (v == null) {
                v = mappingFunction.apply(k);
                put(k, v, aliveTime, callback);
            }
            return v;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public V remove(K k) {
        lock.writeLock().lock();
        try {
            ExpireValue<V> val = dataMap.remove(k);
            return val == null ? null : val.getVal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            this.dataMap.clear();
            this.expireKeyList.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean contains(K k){
        ExpireValue<V> expireValue;
        lock.readLock().lock();
        try {
            expireValue = dataMap.get(k);
        }finally {
            lock.readLock().unlock();
        }
        if (expireValue == null) {
            return false;
        } else {
            if (expireValue.isExpired()) {
                V v=remove(k);
                if(v!=null) {
                    callback(k, expireValue);
                }
                return false;
            } else {
                return true;
            }
        }
    }

    public int size(){
        lock.readLock().lock();
        try {
            return dataMap.size();
        }finally {
            lock.readLock().unlock();
        }
    }

    public Set<K> keySet(){
        lock.readLock().lock();
        try {
            return dataMap.keySet();
        }finally {
            lock.readLock().unlock();
        }
    }

    public Collection<V> values(){
        lock.readLock().lock();
        try {
            return dataMap.values().stream()
                    .map(e->e.isExpired()?null:e.getVal())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        }finally {
            lock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExpireThreadSafeMap<String, String> map = new ExpireThreadSafeMap<>(1000L);
        map.init();
        for (int i = 1; i <= 100000; i++) {
            map.put("test" + i, "test1", 2000L, (k, v) -> {
                System.out.println(k + "已经过期了");
            });
//            map.put("test2", "test2", 5000L, (k, v) -> {
//                System.out.println(v + "已经过期了");
//            });
        }
    }

}

@SuppressWarnings("unchecked")
class ExpireKeyLinkedList<K, V> {
    transient Node first;

    public ExpireKeyLinkedList() {
    }

    public void add(ExpireKey<K, V> e) {
        if (first == null) {
            first = new Node<>(null, e, null);
        } else {
            Node<ExpireKey> prev = null;
            Node<ExpireKey> next = first;
            while (true) {
                Long t1 = e.getExpireValue().getExpireTime();
                Long t2 = next.item.getExpireValue().getExpireTime();
                if (t1 > t2) {
                    prev = next;
                    next = prev.next;
                    if (next == null) {
                        prev.next = new Node<>(prev, e, null);
                        break;
                    }
                } else {
                    Node<ExpireKey> cur = new Node<>(prev, e, next);
                    if (prev == null) {
                        first = cur;
                    } else {
                        prev.next = cur;
                    }
                    next.prev = cur;
                    break;
                }
            }
        }
    }

    public List<ExpireKey<K, V>> removeExpired(long ts) {
        List<ExpireKey<K, V>> resList = new ArrayList<>();
        if (first == null) {
            return resList;
        } else {
            Node<ExpireKey<K, V>> cur = first;
            while (cur != null) {
                boolean expired = cur.item.getExpireValue().getExpireTime() <= ts;
                if (expired) {
                    resList.add(cur.item);
                    cur = cur.next;
                } else {
                    break;
                }
            }
            if (cur != null) {
                cur.prev = null;
            }
            first = cur;
            return resList;
        }
    }

    public void clear() {
        first = null;
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
