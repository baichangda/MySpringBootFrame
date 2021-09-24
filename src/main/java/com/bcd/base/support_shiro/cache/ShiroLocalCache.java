package com.bcd.base.support_shiro.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 *
 * @param <K>
 * @param <V>
 */
public class ShiroLocalCache<K, V> implements Cache<K, V> {

    Logger logger = LoggerFactory.getLogger(ShiroLocalCache.class);

    com.github.benmanes.caffeine.cache.Cache<K,V> cache;

    public ShiroLocalCache(Duration expired) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(expired)
                .softValues()
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    @Override
    public V get(K k) throws CacheException {
        return cache.getIfPresent(k);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V oldV=cache.getIfPresent(k);
        cache.put(k, v);
        return oldV;
    }

    @Override
    public V remove(K k) throws CacheException {
        V oldV=cache.getIfPresent(k);
        if(oldV==null){
            return null;
        }else{
            cache.invalidate(k);
            return oldV;
        }
    }

    @Override
    public void clear() throws CacheException {
        cache.invalidateAll();
    }

    @Override
    public int size() {
        return (int) cache.estimatedSize();
    }

    @Override
    public Set<K> keys() {
        return cache.asMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return cache.asMap().values();
    }
}
