package com.bcd.base.map;


import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Map<K, ExpireValue<V>> dataMap = new ConcurrentHashMap<>();


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
            Iterator<Map.Entry<K,ExpireValue<V>>> it=dataMap.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<K,ExpireValue<V>> cur=it.next();
                ExpireValue<V> expireValue =cur.getValue();
                if(expireValue.isExpired()){
                    it.remove();
                    callback(cur.getKey(), expireValue);
                }
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
    public ExpireThreadSafeMap(Long delay, ExecutorService expireWorkPool) {
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
        ExpireValue<V> expireValue= dataMap.get(k);
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
        ExpireValue<V> expireValue = new ExpireValue<>(System.currentTimeMillis() + aliveTime, v, callback);
        ExpireValue<V> val = dataMap.put(k, expireValue);
        return val == null ? null : val.getVal();
    }

    public V putIfAbsent(K k, V v, long aliveTime) {
        return put(k, v, aliveTime, null);
    }

    public V putIfAbsent(K k, V v, long aliveTime, BiConsumer<K, V> callback) {
        ExpireValue<V> expireValue = new ExpireValue<>(System.currentTimeMillis() + aliveTime, v, callback);
        ExpireValue<V> val = dataMap.putIfAbsent(k, expireValue);
        return val == null ? null : val.getVal();
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime) {
        return computeIfAbsent(k, mappingFunction, aliveTime, null);
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime, BiConsumer<K, V> callback) {
        V v = get(k);
        if (v == null) {
            v = mappingFunction.apply(k);
            put(k, v, aliveTime, callback);
        }
        return v;
    }

    public V remove(K k) {
        ExpireValue<V> val = dataMap.remove(k);
        return val == null ? null : val.getVal();
    }

    public void clear() {
        this.dataMap.clear();
    }

    public boolean contains(K k){
        ExpireValue<V> expireValue = dataMap.get(k);
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
        return dataMap.size();
    }

    public Set<K> keySet(){
        return dataMap.keySet();
    }

    public Collection<V> values(){
        return dataMap.values().stream()
                .map(e->e.isExpired()?null:e.getVal())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void main(String[] args) throws InterruptedException {
        ExpireThreadSafeMap<String, String> map = new ExpireThreadSafeMap<>(1*1000L);
        map.init();
        long t1=System.currentTimeMillis();
        for (int i = 1; i <= 100000; i++) {
            map.put("test" + i, "test1", 2000L);
        }
        long t2=System.currentTimeMillis();
        System.out.println(t2-t1);
        AtomicInteger count=new AtomicInteger();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
                System.out.println(count.getAndSet(0));
             },1,1,TimeUnit.SECONDS);
        Executors.newSingleThreadExecutor().execute(()->{
            while(true){
                String val= map.get("test3333");
                count.incrementAndGet();
            }
        });
        while(true){
            long t=System.currentTimeMillis();
            for(int i=1;i<=1000000;i++){
                map.put((t+i)+"",(t+i)+"",2000L);
            }
            long tt=System.currentTimeMillis();
            Thread.sleep(1000);
            System.out.println("======"+(tt-t));
        }
    }

}
