package com.bcd.config.shiro;

import com.bcd.base.map.ExpireConcurrentHashMap;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

/**
 * redis缓存管理器
 * 其中有两级缓存(本地过期缓存、redis缓存)
 * 避免高频率访问redis
 * @param <K>
 * @param <V>
 */
public class RedisCache<K,V> implements Cache<K,V> {

    Logger logger= LoggerFactory.getLogger(RedisCache.class);

    long localTimeout;

    long localScanPeriod;

    ExpireConcurrentHashMap<K,V> map;

    String key;

    RedisTemplate<String, String> redisTemplate;

    BoundHashOperations<String, K, V> boundHashOperations;

    /**
     *
     * @param redisTemplate
     * @param key
     * @param localTimeout 本地缓存失效时间
     * @param localScanPeriod 本地缓存扫描器间隔
     */
    public RedisCache(RedisTemplate<String, String> redisTemplate,String key,long localTimeout,long localScanPeriod) {
        this.redisTemplate = redisTemplate;
        this.boundHashOperations=redisTemplate.boundHashOps(key);
        this.key=key;
        this.localTimeout=localTimeout;
        this.localScanPeriod=localScanPeriod;
        this.map=new ExpireConcurrentHashMap<>(localScanPeriod);
        this.map.init();
    }

    @Override
    public V get(K k) throws CacheException {
        return map.computeIfAbsent(k,e->{
            return boundHashOperations.get(k);
        },localTimeout);
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V old=get(k);
        map.put(k,v,localTimeout);
        boundHashOperations.put(k,v);
        return old;
    }

    @Override
    public V remove(K k) throws CacheException {
        V old=get(k);
        map.remove(k);
        boundHashOperations.delete(k);
        return old;
    }

    @Override
    public void clear() throws CacheException {
        map.clear();
        redisTemplate.delete(key);
    }

    @Override
    public int size() {
        return boundHashOperations.size().intValue();
    }

    @Override
    public Set<K> keys() {
        return boundHashOperations.keys();
    }

    @Override
    public Collection<V> values() {
        return boundHashOperations.values();
    }
}
