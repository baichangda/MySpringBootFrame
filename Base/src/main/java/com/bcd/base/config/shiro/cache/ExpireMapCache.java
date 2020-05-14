package com.bcd.base.config.shiro.cache;

import com.bcd.base.map.ExpireSoftReferenceConcurrentHashMap;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

public class ExpireMapCache<K,V> implements Cache<K,V> {

    public ExpireSoftReferenceConcurrentHashMap<K,V> map;

    //ms
    long timeout;

    //ms
    long scanPeriod;

    String name;

    public ExpireMapCache(long timeout, long scanPeriod) {
        this.timeout = timeout;
        this.scanPeriod=scanPeriod;
        this.map=new ExpireSoftReferenceConcurrentHashMap<>(scanPeriod);
        this.map.init();
    }

    @Override
    public V get(K k) throws CacheException {
        return map.get(k);
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
