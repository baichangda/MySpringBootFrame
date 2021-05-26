package com.bcd.base.support_shiro;

import com.bcd.base.support_shiro.cache.ShiroLocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class LocalCacheManager extends AbstractCacheManager {
    long expired;

    TimeUnit unit;

    /**
     * @param expired key过期时间
     * @param unit
     */
    public LocalCacheManager(long expired, TimeUnit unit) {
        this.expired = expired;
        this.unit = unit;
    }

    @Override
    protected Cache createCache(String s) throws CacheException {
        return new ShiroLocalCache(expired, unit);
    }
}
