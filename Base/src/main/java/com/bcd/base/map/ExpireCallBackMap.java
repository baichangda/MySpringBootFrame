package com.bcd.base.map;


import com.bcd.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 可以插入过期key-value的 Map
 * 插入的key-value会在aliveTime后被移除
 * 过期策略:
 * 1、懒汉模式: 在调用get时候检查,如果过期则移除
 * 2、定期检查模式: 启动计划任务执行器周期性的检查所有此类的实例,检查并移除里面过期的key
 * 在过期被移除后,会调用设置的过期回调方法
 *
 * 适用于绑定过期回调
 * 如果作为缓存可能会导致内存溢出
 *
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireCallBackMap<V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireCallBackMap.class);
    private final Map<String, Value<V>> dataMap = new HashMap<>();

    /**
     * 用于从map中检索出过期key并移除 定时任务线程池
     */
    private ScheduledExecutorService expireScanPool;
    private Long delay;
    private TimeUnit unit;

    /**
     * 用于执行过期的回调方法线程池
     * 为null则不触发回调
     */
    private ExecutorService expireWorkPool;

    private void initExpiredSchedule() {
        expireScanPool=Executors.newSingleThreadScheduledExecutor();
        expireScanPool.scheduleWithFixedDelay(this::scanAndClearExpired, delay, delay, unit);
    }

    /**
     * 扫描并且清除过期值
     * 会触发回调
     */
    public void scanAndClearExpired(){
        Iterator<Map.Entry<String, Value<V>>> it=dataMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Value<V>> cur=it.next();
            synchronized (cur.getKey().intern()) {
                Value<V> expireValue = cur.getValue();
                if (expireValue.isExpired()) {
                    it.remove();
                    callback(cur.getKey(), expireValue.getVal(), expireValue.getCallback());
                }
            }
        }
    }

    /**
     * 不开启扫描和回调
     */
    public ExpireCallBackMap(){
        this(null,null);
    }

    /**
     * @param delay 扫描计划执行间隔,为null表示不开启扫描
     */
    public ExpireCallBackMap(Long delay,TimeUnit unit) {
        this(delay,unit,null);
    }

    /**
     * @param delay          扫描计划执行间隔,为null则表示不进行扫描
     * @param unit          扫描计划执行间隔,为null则表示不进行扫描
     * @param expireWorkPool 过期回调执行线程池 传入null代表不触发回调
     */
    public ExpireCallBackMap(Long delay,TimeUnit unit, ExecutorService expireWorkPool) {
        this.delay = delay;
        this.unit = unit;
        this.expireWorkPool = expireWorkPool;
    }

    public void init(){
        if(delay !=null&&unit!=null){
            initExpiredSchedule();
        }
    }

    public void destroy(){
        if(expireScanPool!=null){
            expireScanPool.shutdown();
        }
    }



    private void callback(String k, V v,BiConsumer callBack) {
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
     * 获取值
     * @param expireValue
     * @param k
     * @param triggerCallback
     * @return
     */
    private V getVal(Value<V> expireValue, String k,boolean triggerCallback){
        if (expireValue == null) {
            return null;
        } else {
            if (expireValue.isExpired()) {
                //移除元素、标记
                dataMap.remove(k);
                //触发回调
                if(triggerCallback){
                    V v = expireValue.getVal();
                    BiConsumer callBack = expireValue.getCallback();
                    callback(k, v, callBack);
                }
                return null;
            } else {
                return expireValue.getVal();
            }
        }
    }


    /**
     * 过期检查且触发回调
     * @param k
     * @return
     */
    public V get(String k) {
        synchronized (k.intern()){
            Value<V> expireValue= dataMap.get(k);
            return getVal(expireValue,k,true);
        }
    }

    /**
     * put元素、不会过期检查触发回调
     * @param k
     * @param v
     * @param expiredTime
     * @return
     */
    public V put(String k, V v, long expiredTime,TimeUnit unit) {
        return put(k, v, expiredTime,unit, null);
    }

    /**
     * put元素、不会过期检查触发回调
     * @param k
     * @param v
     * @param expiredTime
     * @param callback
     * @return
     */
    public V put(String k, V v, long expiredTime,TimeUnit unit, BiConsumer<String, V> callback) {
        Value<V> expireValue = new Value<>(System.currentTimeMillis() + unit.toMillis(expiredTime), v, callback);
        synchronized (k.intern()) {
            Value<V> oldVal = dataMap.put(k, expireValue);
            return getVal(oldVal, k, false);
        }
    }

    /**
     * remove元素、不会过期检查触发回调
     * @param k
     * @return
     */
    public V remove(String k) {
        synchronized (k.intern()) {
            Value<V> oldVal = dataMap.remove(k);
            return getVal(oldVal, k, false);
        }
    }

    /**
     * removeAll、不会过期检查触发回调
     * @return
     */
    public void removeAll(){
        for (String k : dataMap.keySet()) {
            remove(k);
        }
    }

    private final static class Value<T>{
        private Long expiredTimeInMillis;
        private T val;
        private BiConsumer callback;

        public Value(Long expiredTimeInMillis, T val, BiConsumer callback) {
            this.expiredTimeInMillis = expiredTimeInMillis;
            this.val = val;
            this.callback = callback;
        }

        public T getVal() {
            return val;
        }

        public BiConsumer getCallback() {
            return callback;
        }

        public Long getExpiredTimeInMillis() {
            return expiredTimeInMillis;
        }

        public boolean isExpired() {
            return expiredTimeInMillis < System.currentTimeMillis();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExpireCallBackMap<String> map = new ExpireCallBackMap<>(1L,TimeUnit.SECONDS,Executors.newSingleThreadExecutor());
        map.init();
        map.put("1","2",1,TimeUnit.SECONDS,(k,v)->{
            System.out.println(map.get("1"));
            System.out.println(map.get("2"));
        });
        map.put("2","3",10,TimeUnit.SECONDS,(k,v)->{
            System.out.println(map.get("1"));
            System.out.println(map.get("2"));
        });
    }

}
