package com.bcd.base.cache;

import com.bcd.base.map.MyCache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class LocalCache extends AbstractValueAdaptingCache {

    private String name;
    private MyCache<Object,Object> cache;


    public LocalCache(String name, Long expired,TimeUnit unit) {
        this(name, expired,unit,true);
    }

    public LocalCache(String name, Long expired,TimeUnit unit, boolean allowNullValues) {
        super(allowNullValues);
        this.name = name;
        this.cache =new MyCache<>()
                .expiredAfter(expired, unit)
                .withSoftReferenceValue()
                .withClearExpiredValueExecutor(Executors.newSingleThreadScheduledExecutor(),60,60, TimeUnit.SECONDS)
                .init();

    }

    @Nullable
    @Override
    protected Object lookup(Object key) {
        return this.cache.get(doWithKey(key));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.cache;
    }

    @Nullable
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) fromStoreValue(this.cache.computeIfAbsent(doWithKey(key), r -> {
            try {
                return toStoreValue(valueLoader.call());
            }
            catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        }));
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.cache.put(doWithKey(key),value);
    }

    @Nullable
    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        return toValueWrapper(this.cache.putIfAbsent(doWithKey(key),value));
    }

    @Override
    public void evict(Object key) {
        this.cache.remove(doWithKey(key));
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    private String doWithKey(Object key){
        if(key==null){
            return "";
        }
        return key.toString();
    }

}
