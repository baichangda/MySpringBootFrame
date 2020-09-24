package com.bcd.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 */
@SuppressWarnings("unchecked")
public class LocalCache extends AbstractValueAdaptingCache {

    Logger logger= LoggerFactory.getLogger(LocalCache.class);

    private String name;
    private LoadingCache<Object, Object> cache;


    public LocalCache(String name, Long aliveTimeInSecond) {
        this(name,aliveTimeInSecond,true);
    }

    public LocalCache(String name, Long aliveTimeInSecond, boolean allowNullValues) {
        super(allowNullValues);
        this.cache= CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(aliveTimeInSecond))
                .softValues()
                .build(new CacheLoader<Object, Object>() {
            @Override
            public Object load(Object key) {
                return null;
            }
        });
        this.name = name;
    }

    @Nullable
    @Override
    protected Object lookup(Object key) {
        return this.cache.getIfPresent(doWithKey(key));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.cache;
    }

    /**
     * @param key
     * @param valueLoader 如果此对象返回null、会导致异常
     * @param <T>
     * @return
     */
    @Nullable
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            return (T)fromStoreValue(cache.get(doWithKey(key), ()->toStoreValue(valueLoader.call())));
        } catch (ExecutionException e) {
            logger.error("local cache get error",e);
            return null;
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.cache.put(doWithKey(key),value);
    }

    @Override
    public void evict(Object key) {
        this.cache.invalidate(doWithKey(key));
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    private String doWithKey(Object key){
        if(key==null){
            return "";
        }
        return key.toString();
    }

}
