package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProviderUtil {
    static RedisConnectionFactory redisConnectionFactory;

    static ProviderProp providerProp;

    @Autowired
    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        ProviderUtil.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 根据序号轮询方式获取host
     *
     * @param type
     * @param no
     * @return
     */
    public static String host(String type, int no) {
        final ArrayList<String> hosts = hosts(type);
        if (hosts.isEmpty()) {
            return null;
        }
        return hosts.get(no % hosts.size());
    }

    private static ConcurrentHashMap<String, AtomicInteger> typeToNo = new ConcurrentHashMap<>();

    /**
     * 使用全局静态序号完成
     *
     * @param type
     * @return
     */
    public static String host(String type) {
        return host(type, typeToNo.computeIfAbsent(type, k -> new AtomicInteger()).getAndIncrement());
    }


    static class ProviderInfo {
        long lastUpdateTs;
        ArrayList<String> hosts = new ArrayList<>();

    }

    final static HashMap<String, ProviderInfo> typeToProvider = new HashMap<>();
    final static HashMap<String, BoundHashOperations<String, String, String>> operationsMap = new HashMap<>();

    /**
     * 获取可用host
     *
     * @param type
     * @return
     */
    public static ArrayList<String> hosts(String type) {
        final long curTs = System.currentTimeMillis();
        final long checkTs = curTs - providerProp.expired.toMillis();
        //从缓存中获取提供者信息
        ProviderInfo providerInfo = typeToProvider.get(type);
        //检查信息是否过期
        if (providerInfo == null || providerInfo.lastUpdateTs < checkTs) {
            //过期则从redis中读取
            synchronized (type.intern()) {
                if (providerInfo == null || providerInfo.lastUpdateTs < checkTs) {
                    if (providerInfo == null) {
                        providerInfo = new ProviderInfo();
                    }
                    providerInfo.lastUpdateTs = curTs;

                    //从redis中加载
                    final ArrayList<String> hosts = new ArrayList<>();
                    final BoundHashOperations<String, String, String> boundHashOperations = operationsMap.computeIfAbsent(type, e -> RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(RedisUtil.doWithKey("provider:" + type)));
                    final Map<String, String> entries = boundHashOperations.entries();
                    if (!entries.isEmpty()) {
                        for (Map.Entry<String, String> entry : entries.entrySet()) {
                            final String value = entry.getValue();
                            if (DateZoneUtil.stringToDate_second(value).getTime() >= checkTs) {
                                hosts.add(entry.getKey());
                            }
                        }
                        hosts.sort(String::compareTo);
                        hosts.trimToSize();
                        return hosts;
                    }
                    providerInfo.hosts = hosts;
                }
            }
        }
        return providerInfo.hosts;
    }
}
