package com.bcd.base.config.shiro.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ExpireMapCache<K,V> implements Cache<K,V> {


    public LoadingCache<K,V> map;

    //ms
    long timeoutInSecond;

    //ms
    long scanPeriodInSecond;

    public ExpireMapCache(long timeoutInSecond, long scanPeriodInSecond) {
        this.timeoutInSecond = timeoutInSecond;
        this.scanPeriodInSecond=scanPeriodInSecond;
        this.map= CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(timeoutInSecond))
                .softValues()
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        return null;
                    }
                });
    }

    @Override
    public V get(K k) throws CacheException {
        try {
            return map.get(k);
        } catch (ExecutionException e) {
            logger
            return null;
        }
    }

    @Override
    public V put(K k, V v) throws CacheException {
        return map.put(k,v,timeout);
    }

    @Override
    public V remove(K k) throws CacheException {
        return map.remove(k);
    }

    @Override
    public void clear() throws CacheException {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Set<K> keys() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }
}
