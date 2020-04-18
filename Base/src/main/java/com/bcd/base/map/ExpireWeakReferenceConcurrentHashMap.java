package com.bcd.base.map;


import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 过期的弱引用 线程安全hashMap
 * 适合作为缓存
 *
 * 注意:
 * 在调用如下方法时候会检查过期并触发回调
 * 在调用{@link #get(Object)}、{@link #contains(Object)}}
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireWeakReferenceConcurrentHashMap<K, V> {
    private final static Logger logger = LoggerFactory.getLogger(ExpireWeakReferenceConcurrentHashMap.class);
    private final Map<K, ExpireWeakReferenceValue<K,V>> dataMap = new ConcurrentHashMap<>();

    private final ReferenceQueue<ReferenceData<K,V>> referenceQueue=new ReferenceQueue<>();

    AtomicInteger atomicInteger=new AtomicInteger(0);

    /**
     * 用于从map中检索出过期key并移除 定时任务线程池
     */
    private ScheduledExecutorService expireScanPool;
    private ExecutorService clearWeakDataPool;
    private Long delay;

    private void startClearReferenceDataSchedule(){
        this.clearWeakDataPool = Executors.newSingleThreadExecutor();
        this.clearWeakDataPool.execute(()->{
            try {
                while(true){
                    ExpireWeakReferenceValue<String,String> reference= (ExpireWeakReferenceValue<String,String>)referenceQueue.remove();
                    dataMap.remove(reference.getKey());
                    atomicInteger.incrementAndGet();
                }
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
        });
    }

    private void startExpireSchedule() {
        this.expireScanPool.scheduleAtFixedRate(this::scanAndClearExpired,delay,delay,TimeUnit.MICROSECONDS);
    }


    /**
     * 不开启扫描和回调
     */
    public ExpireWeakReferenceConcurrentHashMap(){
        this(null);
    }

    /**
     * @param delay 扫描计划执行间隔,为null表示不开启扫描
     */
    public ExpireWeakReferenceConcurrentHashMap(Long delay) {
        this.delay=delay;
    }

    public void init(){
        if(delay!=null){
            this.expireScanPool=Executors.newScheduledThreadPool(1);
            startExpireSchedule();
        }
        startClearReferenceDataSchedule();
    }

    public void destroy(){
        if(expireScanPool!=null){
            expireScanPool.shutdown();
        }
    }

    /**
     * 扫描并且清除过期值
     * 会触发回调
     */
    public void scanAndClearExpired(){
        Iterator<Map.Entry<K, ExpireWeakReferenceValue<K,V>>> it=dataMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<K, ExpireWeakReferenceValue<K,V>> cur=it.next();
            ExpireWeakReferenceValue<K,V> expireValue =cur.getValue();
            V val= expireValue.getVal();
            if(val==null){
                it.remove();
            }
        }
    }

    public V get(K k) {
        ExpireWeakReferenceValue<K,V> expireValue= dataMap.get(k);
        if (expireValue == null) {
            return null;
        } else {
            return expireValue.getVal();
        }
    }

    public V put(K k, V v, long aliveTime) {
        ExpireWeakReferenceValue<K,V> expireValue = new ExpireWeakReferenceValue<>(System.currentTimeMillis() + aliveTime,new ReferenceData<>(k,v),referenceQueue);
        ExpireWeakReferenceValue<K,V> oldVal = dataMap.put(k, expireValue);
        return oldVal == null ? null : oldVal.getVal();
    }

    public V putIfAbsent(K k, V v, long aliveTime) {
        ExpireWeakReferenceValue<K,V> expireValue = new ExpireWeakReferenceValue<>(System.currentTimeMillis() + aliveTime,new ReferenceData<>(k,v),referenceQueue);
        ExpireWeakReferenceValue<K,V> oldVal = dataMap.putIfAbsent(k, expireValue);
        return oldVal == null ? null : oldVal.getVal();
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime) {
        Function<? super K, ? extends ExpireWeakReferenceValue<K,V>> function=e->
                new ExpireWeakReferenceValue<>(System.currentTimeMillis()+aliveTime,new ReferenceData<>(k,mappingFunction.apply(e)),referenceQueue);
        ExpireWeakReferenceValue<K,V> oldVal= dataMap.computeIfAbsent(k,function);
        return oldVal == null ? null : oldVal.getVal();
    }

    public V remove(K k) {
        ExpireWeakReferenceValue<K,V> oldVal = dataMap.remove(k);
        return oldVal == null ? null : oldVal.getVal();
    }

    public void clear() {
        this.dataMap.clear();
    }

    public boolean contains(K k){
        ExpireWeakReferenceValue<K,V> expireValue = dataMap.get(k);
        if (expireValue == null) {
            return false;
        } else {
            return Objects.nonNull(expireValue.getVal());
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
                .map(e->e.getVal())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static void main(String[] args) throws InterruptedException {
//        ExpireConcurrentHashMap<String, String> map = new ExpireConcurrentHashMap<>(1*1000L);
        ExpireWeakReferenceConcurrentHashMap<String, byte[]> map = new ExpireWeakReferenceConcurrentHashMap<>();
        map.init();
        long t1=System.currentTimeMillis();
        for (int i = 1; i <= 100; i++) {
            map.put("test" + i, new byte[1024*1024], 1000000L);
        }
        long t2=System.currentTimeMillis();
        System.out.println(t2-t1);
        AtomicInteger count=new AtomicInteger();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
                System.out.println(count.getAndSet(0));
             },1,1,TimeUnit.SECONDS);
        Executors.newSingleThreadExecutor().execute(()->{
            while(true){
                byte[] val= map.get("test3333");
                count.incrementAndGet();
            }
        });
        while(true){
            long t=System.currentTimeMillis();
            for(int i=1;i<=500;i++){
                map.put((t+i)+"",new byte[1024*1024],1000000L);
            }
            long tt=System.currentTimeMillis();
            Thread.sleep(1000);
            System.out.println("======"+(tt-t));

            System.out.println("+++++++++++++++"+map.size());
            System.out.println("---------------"+map.atomicInteger.getAndSet(0));
        }

    }

}
