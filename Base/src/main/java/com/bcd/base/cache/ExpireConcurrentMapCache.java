package com.bcd.base.cache;

import com.bcd.base.map.ExpireConcurrentMap;
import com.bcd.base.util.JsonUtil;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public class ExpireConcurrentMapCache extends AbstractValueAdaptingCache {

    private String name;
    private Long aliveTime;
    private ExpireConcurrentMap<Object,Object> dataMap=new ExpireConcurrentMap<>();


    public ExpireConcurrentMapCache(String name, Long aliveTime) {
        this(name,aliveTime,true);
    }

    public ExpireConcurrentMapCache(String name, Long aliveTime, boolean allowNullValues) {
        super(allowNullValues);
        this.name = name;
        this.aliveTime = aliveTime;
    }

    @Nullable
    @Override
    protected Object lookup(Object key) {
        return this.dataMap.get(doWithKey(key));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.dataMap;
    }

    @Nullable
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) fromStoreValue(this.dataMap.computeIfAbsent(doWithKey(key), r -> {
            try {
                return toStoreValue(valueLoader.call());
            }
            catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        },this.aliveTime));
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.dataMap.put(doWithKey(key),value,aliveTime);
    }

    @Nullable
    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        return toValueWrapper(this.dataMap.putIfAbsent(doWithKey(key),value,aliveTime));
    }

    @Override
    public void evict(Object key) {
        this.dataMap.remove(doWithKey(key));
    }

    @Override
    public void clear() {
        this.dataMap.clear();
    }

    private String doWithKey(Object key){
        if(key==null){
            return "";
        }
        return JsonUtil.toJson(key);
    }

}
