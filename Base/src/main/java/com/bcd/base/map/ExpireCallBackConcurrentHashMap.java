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
 * 插入的key-value会在aliveTime后被移除
 * 过期策略:
 * 1、懒汉模式: 在调用get、contains时候检查,如果过期则移除
 * 2、定期检查模式: 启动计划任务执行器周期性的检查所有此类的实例,检查并移除里面过期的key
 * 在过期被移除后,会调用设置的过期回调方法
 *
 * 适用于绑定过期回调
 * 如果作为缓存可能会导致内存溢出
 *
 * 注意
 * 在调用如下方法时候会检查过期并触发回调
 * 在调用{@link #get(Object)}、{@link #contains(Object)}}
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireCallBackConcurrentHashMap<K, V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireCallBackConcurrentHashMap.class);
    private final Map<K, ExpireCallBackValue<V>> dataMap = new ConcurrentHashMap<>();


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
        expireScanPool.scheduleWithFixedDelay(this::scanAndClearExpired, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 扫描并且清除过期值
     * 会触发回调
     */
    public void scanAndClearExpired(){
        Iterator<Map.Entry<K, ExpireCallBackValue<V>>> it=dataMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<K, ExpireCallBackValue<V>> cur=it.next();
            ExpireCallBackValue<V> expireValue =cur.getValue();
            if(expireValue.isExpired()){
                it.remove();
                callback(cur.getKey(),expireValue.getVal(), expireValue.getCallback());
            }
        }
    }

    /**
     * 不开启扫描和回调
     */
    public ExpireCallBackConcurrentHashMap(){
        this(null,null);
    }

    /**
     * @param delay 扫描计划执行间隔,为null表示不开启扫描
     */
    public ExpireCallBackConcurrentHashMap(Long delay) {
        this(delay,null);
    }

    /**
     * @param delay          扫描计划执行间隔,为null则表示不进行扫描
     * @param expireWorkPool 过期回调执行线程池 传入null代表不触发回调
     */
    public ExpireCallBackConcurrentHashMap(Long delay, ExecutorService expireWorkPool) {
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



    private void callback(K k, V v,BiConsumer callBack) {
        if(expireWorkPool!=null) {
            if (callBack != null) {
                expireWorkPool.execute(() -> {
                    try {
                        callBack.accept(k, v);
                    } catch (Exception e) {
                        ExceptionUtil.printException(e);
                    }
                });
            }
        }
    }


    /**
     * 获取值,触发回调
     * @param expireValue
     * @param k
     * @return
     */
    private V getVal(ExpireCallBackValue<V> expireValue,K k){
        if (expireValue == null) {
            return null;
        } else {
            if (expireValue.isExpired()) {
                dataMap.remove(k);
                V v = expireValue.getVal();
                BiConsumer callBack=expireValue.getCallback();
                callback(k, v,callBack);
                return null;
            } else {
                return expireValue.getVal();
            }
        }
    }

    /**
     * 获取值,不触发回调
     * @param expireValue
     */
    private V getVal(ExpireCallBackValue<V> expireValue){
        if (expireValue == null) {
            return null;
        } else {
            if (expireValue.isExpired()) {
                return null;
            } else {
                return expireValue.getVal();
            }
        }
    }


    /**
     * 过期则触发回调
     * @param k
     * @return
     */
    public V get(K k) {
        ExpireCallBackValue<V> expireValue= dataMap.get(k);
        return getVal(expireValue,k);
    }

    /**
     * 不触发回调
     * @param k
     * @param v
     * @param aliveTime
     * @return
     */
    public V put(K k, V v, long aliveTime) {
        return put(k, v, aliveTime, null);
    }

    /**
     * 不触发回调
     * @param k
     * @param v
     * @param aliveTime
     * @param callback
     * @return
     */
    public V put(K k, V v, long aliveTime, BiConsumer<K, V> callback) {
        ExpireCallBackValue<V> expireValue = new ExpireCallBackValue<>(System.currentTimeMillis() + aliveTime, v, callback);
        ExpireCallBackValue<V> oldVal = dataMap.put(k, expireValue);
        return getVal(oldVal);
    }

    /**
     * 不触发回调
     * @param k
     * @param v
     * @param aliveTime
     * @return
     */
    public V putIfAbsent(K k, V v, long aliveTime) {
        return put(k, v, aliveTime, null);
    }

    /**
     * 不触发回调
     * @param k
     * @param v
     * @param aliveTime
     * @param callback
     * @return
     */
    public V putIfAbsent(K k, V v, long aliveTime, BiConsumer<K, V> callback) {
        ExpireCallBackValue<V> expireValue = new ExpireCallBackValue<>(System.currentTimeMillis() + aliveTime, v, callback);
        ExpireCallBackValue<V> oldVal = dataMap.putIfAbsent(k, expireValue);
        return getVal(oldVal);
    }

    /**
     * 不触发回调
     * @param k
     * @param mappingFunction
     * @param aliveTime
     * @return
     */
    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime) {
        return computeIfAbsent(k, mappingFunction, aliveTime, null);
    }

    /**
     * 不触发回调
     * @param k
     * @param mappingFunction
     * @param aliveTime
     * @param callback
     * @return
     */
    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime, BiConsumer<K, V> callback) {
        Function<? super K, ? extends ExpireCallBackValue<V>> function=e->
             new ExpireCallBackValue<>(System.currentTimeMillis()+aliveTime,mappingFunction.apply(e),callback);
        ExpireCallBackValue<V> oldVal= dataMap.computeIfAbsent(k,function);
        return getVal(oldVal);
    }

    /**
     * 不触发回调
     * @param k
     * @return
     */
    public V remove(K k) {
        ExpireCallBackValue<V> oldVal = dataMap.remove(k);
        return getVal(oldVal);
    }

    /**
     * 不触发回调
     */
    public void clear() {
        this.dataMap.clear();
    }

    /**
     * 过期则触发回调
     * @param k
     * @return
     */
    public boolean contains(K k){
        ExpireCallBackValue<V> expireValue = dataMap.get(k);
        return getVal(expireValue,k)!=null;
    }

    public int size(){
        return dataMap.size();
    }

    public Set<K> keySet(){
        return dataMap.keySet();
    }

    public Collection<V> values(){
        return dataMap.values().stream()
                .map(e->e.getVal())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }



    public static void main(String[] args) throws InterruptedException {
        ExpireCallBackConcurrentHashMap<String, String> map = new ExpireCallBackConcurrentHashMap<>(1*1000L);
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
