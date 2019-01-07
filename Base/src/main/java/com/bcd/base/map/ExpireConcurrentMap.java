package com.bcd.base.map;


import com.bcd.base.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 可以插入过期key-value的 ConcurrentHashMap
 * 插入的key-value会在aliveTime后被移除,如果aliveTime为-1则表明不过期
 * 过期策略:
 * 1、懒汉模式: 在调用get时候检查,如果过期则移除
 * 2、定期检查模式: 启动计划任务执行器周期性的检查所有此类的实例,检查并移除里面过期的key
 *
 * 在过期被移除后,会调用设置的过期回调方法
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class ExpireConcurrentMap<K,V> {
    private final static Logger logger= LoggerFactory.getLogger(ExpireConcurrentMap.class);
    private final ConcurrentHashMap<K,ExpireValue<V>> dataMap=new ConcurrentHashMap<>();

    public ExpireConcurrentMap() {
        ExpireWorker.EXPIRE_MAP_LIST.add(this);
    }

    private void callback(K k,ExpireValue<V> expireValue){
        if(expireValue.getCallback()!=null){
            try {
                expireValue.getCallback().accept(k, expireValue.getVal());
            }catch (Exception e){
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    public V get(K k){
       ExpireValue<V> expireValue= dataMap.get(k);
       if(expireValue==null){
           return null;
       }else {
           if (expireValue.isExpired()) {
               dataMap.remove(k);
               callback(k,expireValue);
               return null;
           } else {
               return expireValue.getVal();
           }
       }
    }

    public V put(K k,V v,long aliveTime){
        ExpireValue<V> val=dataMap.put(k,new ExpireValue<>(System.currentTimeMillis(),aliveTime,v));
        return val==null?null:val.getVal();
    }

    public V put(K k, V v, long aliveTime, BiConsumer<K,V> callback){
        ExpireValue<V> val=dataMap.put(k,new ExpireValue<>(System.currentTimeMillis(),aliveTime,v,callback));
        return val==null?null:val.getVal();
    }

    public V putIfAbsent(K k,V v,long aliveTime){
        ExpireValue<V> val=dataMap.putIfAbsent(k,new ExpireValue<>(System.currentTimeMillis(),aliveTime,v));
        return val==null?null:val.getVal();
    }

    public V putIfAbsent(K k,V v,long aliveTime, BiConsumer<K,V> callback){
        ExpireValue<V> val=dataMap.putIfAbsent(k,new ExpireValue<>(System.currentTimeMillis(),aliveTime,v,callback));
        return val==null?null:val.getVal();
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime){
        ExpireValue<V> val=dataMap.computeIfAbsent(k,e->new ExpireValue<>(System.currentTimeMillis(),aliveTime,mappingFunction.apply(e)));
        return val==null?null:val.getVal();
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> mappingFunction, long aliveTime,BiConsumer<K,V> callback){
        ExpireValue<V> val=dataMap.computeIfAbsent(k,e->new ExpireValue<>(System.currentTimeMillis(),aliveTime,mappingFunction.apply(e),callback));
        return val==null?null:val.getVal();
    }

    public V remove(K k){
        ExpireValue<V> val=dataMap.remove(k);
        return val==null?null:val.getVal();
    }

    public void clear(){
        this.dataMap.clear();
    }

    static class ExpireWorker {
        private ExpireWorker() {
        }

        /**
         * 用于存放所有的ExpireConcurrentMap的实例
         */
        private static List<ExpireConcurrentMap> EXPIRE_MAP_LIST=new ArrayList<>();
        /**
         * 用于从map中检索出过期key并移除
         */
        private static ScheduledExecutorService EXPIRE_SES= Executors.newScheduledThreadPool(1);
        /**
         * 用于执行过期的回调方法线程池
         */
        private static ExecutorService EXPIRE_WORK_POOL= Executors.newCachedThreadPool();
        static {
            EXPIRE_SES.scheduleWithFixedDelay(()->
                EXPIRE_MAP_LIST.forEach(map->
                    map.dataMap.forEach((k,v)->{
                        if(((ExpireValue)v).isExpired()){
                            map.dataMap.remove(k);
                            EXPIRE_WORK_POOL.execute(()-> map.callback(k,(ExpireValue) v));
                        }
                    })
                )
            ,1000L,1000L, TimeUnit.MILLISECONDS);
        }
    }

    public static void main(String [] args) throws InterruptedException {
        ExpireConcurrentMap<String,Object> map=new ExpireConcurrentMap<>();
        BiConsumer biConsumer=(k,v)->logger.debug("已过期:{} {}",k,v);
        map.put("a","a",1000L,biConsumer);
        map.put("b","b",5000L,biConsumer);
        logger.debug((String)map.get("a"));
        logger.debug((String)map.get("b"));
        Thread.sleep(1000L);
        Thread.sleep(5000L);
        logger.debug((String) map.get("b"));
    }

}
