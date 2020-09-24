package com.bcd.config.shiro;

import com.bcd.base.config.shiro.cache.LocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

@SuppressWarnings("unchecked")
public class ExpireMapCacheManager extends AbstractCacheManager{
    long timeoutInSecond;
    long scanPeriodInSecond;

    public ExpireMapCacheManager(long timeoutInSecond) {
        this(timeoutInSecond,3*60);
    }

    /**
     * @param timeoutInSecond key过期时间
     * @param scanPeriodInSecond 扫描线程运行周期
     */
    public ExpireMapCacheManager(long timeoutInSecond, long scanPeriodInSecond) {
        this.timeoutInSecond = timeoutInSecond;
        this.scanPeriodInSecond = scanPeriodInSecond;
    }
    @Override
    protected Cache createCache(String s) throws CacheException {
        return new LocalCache(timeoutInSecond, scanPeriodInSecond);
    }
}
