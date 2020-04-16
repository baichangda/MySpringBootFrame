package com.bcd.config.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

public class RedisCache<K,V> implements Cache<K,V> {

    private static String hashKey="shiroCache";

    RedisTemplate<String, String> redisTemplate;

    BoundHashOperations<String, K, V> boundHashOperations;

    public RedisCache(RedisTemplate<String, String> redisTemplate,String key) {
        this.redisTemplate = redisTemplate;
        this.boundHashOperations=redisTemplate.boundHashOps(key);
    }

    @Override
    public V get(K k) throws CacheException {
        return (V)boundHashOperations.get(k.toString());
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V old=get(k);
        boundHashOperations.put(k,v);
        return old;
    }

    @Override
    public V remove(K k) throws CacheException {
        V old=get(k);
        boundHashOperations.delete(k);
        return old;
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(hashKey);
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
