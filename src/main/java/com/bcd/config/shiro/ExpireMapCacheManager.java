package com.bcd.config.shiro;

import com.bcd.base.config.shiro.cache.ExpireMapCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

@SuppressWarnings("unchecked")
public class ExpireMapCacheManager extends AbstractCacheManager{
    long timeout;
    long scanPeriod;

    public ExpireMapCacheManager(long timeout) {
        this(timeout,3*60*1000L);
    }

    /**
     * @param timeout key过期时间
     * @param scanPeriod 扫描线程运行周期
     */
    public ExpireMapCacheManager(long timeout, long scanPeriod) {
        this.timeout=timeout;
        this.scanPeriod=scanPeriod;
    }
    @Override
    protected Cache createCache(String s) throws CacheException {
        return new ExpireMapCache(timeout,scanPeriod);
    }
}
