package com.bcd.base.support_shiro;

import com.bcd.base.support_shiro.cache.ShiroLocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class LocalCacheManager extends AbstractCacheManager {
    private final Duration expired;

    /**
     * @param expired key过期时间
     */
    public LocalCacheManager(Duration expired) {
        this.expired = expired;
    }

    @Override
    protected Cache createCache(String s) throws CacheException {
        return new ShiroLocalCache(expired);
    }
}
