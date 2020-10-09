package com.bcd.base.config.shiro.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

/**
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 * @param <K>
 * @param <V>
 */
public class LocalCache<K,V> implements Cache<K,V> {

    Logger logger= LoggerFactory.getLogger(LocalCache.class);

    public LoadingCache<K,V> cache;

    //s
    long timeoutInSecond;

    //s
    long scanPeriodInSecond;


    public LocalCache(long timeoutInSecond, long scanPeriodInSecond) {
        this.timeoutInSecond = timeoutInSecond;
        this.scanPeriodInSecond = scanPeriodInSecond;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(timeoutInSecond))
                .softValues()
                .refreshAfterWrite(Duration.ofSeconds(scanPeriodInSecond))
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        return null;
                    }
                });
    }

    @Override
    public V get(K k) throws CacheException {
        return cache.getIfPresent(k);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V old= get(k);
        cache.put(k,v);
        return old;
    }

    @Override
    public V remove(K k) throws CacheException {
        V old= get(k);
        cache.invalidate(k);
        return old;
    }

    @Override
    public void clear() throws CacheException {
        cache.invalidateAll();
    }

    @Override
    public int size() {
        return (int) cache.size();
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