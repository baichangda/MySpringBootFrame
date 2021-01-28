package com.bcd.base.config.shiro.cache;

import com.bcd.base.map.MyCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 * @param <K>
 * @param <V>
 */
public class LocalCache<K,V> implements Cache<K,V> {

    Logger logger= LoggerFactory.getLogger(LocalCache.class);

    MyCache<K,V> cache;

    public LocalCache(long expiredInMills) {
        this.cache = new MyCache<K,V>()
                .expiredAfter(expiredInMills, TimeUnit.MILLISECONDS)
                .withClearExpiredValueExecutor(Executors.newSingleThreadScheduledExecutor(),60,60,TimeUnit.MINUTES)
                .init();
    }

    @Override
    public V get(K k) throws CacheException {
        return cache.get(k);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        return cache.put(k,v);
    }

    @Override
    public V remove(K k) throws CacheException {
        return cache.remove(k);
    }

    @Override
    public void clear() throws CacheException {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        return cache.keySet();
    }

    @Override
    public Collection<V> values() {
        return cache.values();
    }
}
