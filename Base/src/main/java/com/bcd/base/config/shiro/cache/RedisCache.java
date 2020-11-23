package com.bcd.base.config.shiro.cache;

import com.bcd.base.exception.BaseRuntimeException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * redis缓存管理器
 * 其中有两级缓存(本地过期缓存、redis缓存)
 * 避免高频率访问redis
 *
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 *
 * @param <K>
 * @param <V>
 */
public class RedisCache<K,V> implements Cache<K,V> {

    Logger logger= LoggerFactory.getLogger(RedisCache.class);

    long localTimeout;

    LoadingCache<K,V> cache;

    String key;

    RedisTemplate<String, String> redisTemplate;

    BoundHashOperations<String, K, V> boundHashOperations;

    /**
     *
     * @param redisTemplate
     * @param key
     * @param localTimeoutInSecond 本地缓存失效时间
     */
    public RedisCache(RedisTemplate<String, String> redisTemplate,String key,long localTimeoutInSecond) {
        this.redisTemplate = redisTemplate;
        this.boundHashOperations=redisTemplate.boundHashOps(key);
        this.key=key;
        this.localTimeout=localTimeoutInSecond;
        this.cache= CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(localTimeoutInSecond))
                .softValues()
                .refreshAfterWrite(Duration.ofSeconds(localTimeoutInSecond))
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        logger.info("redis cache load [{}]",key);
                        return boundHashOperations.get(key);
                    }
                });
    }

    @Override
    public V get(K k) throws CacheException {
        try {
            return cache.get(k);
        } catch (ExecutionException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    @Override
    public V put(K k, V v) throws CacheException {
        V old = get(k);
        boundHashOperations.put(k, v);
        cache.put(k, v);
        return old;
    }

    @Override
    public V remove(K k) throws CacheException {
        V old=get(k);
        boundHashOperations.delete(k);
        cache.invalidate(k);
        return old;
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(key);
        cache.invalidateAll();
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
