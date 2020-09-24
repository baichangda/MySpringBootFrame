package com.bcd.config.shiro;

import com.bcd.base.config.shiro.cache.LocalCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

@SuppressWarnings("unchecked")
public class LocalCacheManager extends AbstractCacheManager{
    long timeoutInSecond;
    long scanPeriodInSecond;

    public LocalCacheManager(long timeoutInSecond) {
        this(timeoutInSecond,3*60);
    }

    /**
     * @param timeoutInSecond key过期时间
     * @param scanPeriodInSecond 扫描线程运行周期
     */
    public LocalCacheManager(long timeoutInSecond, long scanPeriodInSecond) {
        this.timeoutInSecond = timeoutInSecond;
        this.scanPeriodInSecond = scanPeriodInSecond;
    }
    @Override
    protected Cache createCache(String s) throws CacheException {
        return new LocalCache(timeoutInSecond, scanPeriodInSecond);
    }
}
