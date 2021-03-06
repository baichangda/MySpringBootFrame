package com.bcd.base.support_shiro.cache;

import com.bcd.base.support_redis.RedisUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis缓存管理器
 * 其中有两级缓存(本地过期缓存、redis缓存)
 * 避免高频率访问redis
 * <p>
 * 缓存对一致性要求并不高、保证最终一致性、所以没有加锁
 *
 * @param <K>
 * @param <V>
 */
public class ShiroRedisCache<K, V> implements Cache<K, V> {

    Logger logger = LoggerFactory.getLogger(ShiroRedisCache.class);

    String key;

    RedisTemplate<String, String> redisTemplate;

    BoundHashOperations<String, K, V> boundHashOperations;


    com.github.benmanes.caffeine.cache.Cache<K,V> cache;

    /**
     * @param redisTemplate
     * @param key
     * @param localExpired  本地缓存失效时间
     */
    public ShiroRedisCache(RedisTemplate<String, String> redisTemplate, String key, long localExpired, TimeUnit unit) {
        this.redisTemplate = redisTemplate;
        this.boundHashOperations = redisTemplate.boundHashOps(RedisUtil.doWithKey(key));
        this.key = key;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(localExpired, unit)
                .expireAfterAccess(localExpired, unit)
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    @Override
    public V get(K k) throws CacheException {
        logger.info("local cache[{}]",k);
        return cache.get(k,e -> {
            logger.info("load from redis cache name[{}] key[{}]", key, e);
            return boundHashOperations.get(e);
        });
    }

    @Override
    public V put(K k, V v) throws CacheException {
        logger.info("put name[{}] key[{}]", key, k);
        V old = boundHashOperations.get(k);
        boundHashOperations.put(k, v);
        cache.put(k, v);
        return old;
    }

    @Override
    public V remove(K k) throws CacheException {
        logger.info("remove name[{}] key[{}]", key, k);
        V old = boundHashOperations.get(k);
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
