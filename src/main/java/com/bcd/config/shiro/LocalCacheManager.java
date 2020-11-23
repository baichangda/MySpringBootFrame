package com.bcd.config.shiro;

import com.bcd.base.config.shiro.cache.LocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

@SuppressWarnings("unchecked")
public class LocalCacheManager extends AbstractCacheManager{
    long timeoutInSecond;

    /**
     * @param timeoutInSecond key过期时间
     */
    public LocalCacheManager(long timeoutInSecond) {
        this.timeoutInSecond = timeoutInSecond;
    }
    @Override
    protected Cache createCache(String s) throws CacheException {
        return new LocalCache(timeoutInSecond);
    }
}
