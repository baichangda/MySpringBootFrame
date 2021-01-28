package com.bcd.config.shiro;

import com.bcd.base.config.shiro.cache.LocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

@SuppressWarnings("unchecked")
public class LocalCacheManager extends AbstractCacheManager{
    long expiredInMills;

    /**
     * @param expiredInMills key过期时间
     */
    public LocalCacheManager(long expiredInMills) {
        this.expiredInMills = expiredInMills;
    }
    @Override
    protected Cache createCache(String s) throws CacheException {
        return new LocalCache(expiredInMills);
    }
}
