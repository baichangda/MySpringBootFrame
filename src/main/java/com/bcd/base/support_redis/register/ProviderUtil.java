package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
     * 获取所有可用的host
     *
     * @param type
     * @return
     */
    public static ArrayList<String> hosts(String type) {
        return Singleton.instance.hosts(type);
    }

    /**
     * 根据序号轮询方式获取host
     *
     * @param type
     * @param no
     * @return
     */
    public static String host(String type, int no) {
        final ArrayList<String> hosts = Singleton.instance.hosts(type);
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


    enum Singleton {
        instance;
        final ScheduledExecutorService consumerPool = Executors.newSingleThreadScheduledExecutor();
        final ConcurrentHashMap<String, ArrayList<String>> typeToHosts = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, BoundHashOperations<String, String, String>> operationsMap = new ConcurrentHashMap<>();

        public ArrayList<String> hosts(String type) {
            final BoundHashOperations<String, String, String> boundHashOperations = operationsMap.computeIfAbsent(type, k -> RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(RedisUtil.doWithKey("provider:" + type)));
            return typeToHosts.computeIfAbsent(type, k -> {
                final Map<String, String> entries = boundHashOperations.entries();
                if (entries.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    for (Map.Entry<String, String> entry : entries.entrySet()) {
                        final String value = entry.getValue();
                        final long diff = DateZoneUtil.stringToDate_second(value).getTime() - System.currentTimeMillis();
                        if (diff <= providerProp.expired.toMillis()) {
                            list.add(entry.getKey());
                        }
                    }
                    //排序
                    list.sort(String::compareTo);
                    //trim
                    list.trimToSize();
                    return list;
                }
            });
        }

        Singleton() {
            final long l = providerProp.expired.toMillis() / 2 + 1000;
            consumerPool.scheduleAtFixedRate(() -> {
                operationsMap.forEach((k, v) -> {
                    final Map<String, String> entries = v.entries();
                    final ArrayList<String> local = typeToHosts.get(k);
                    final ArrayList<String> remote = new ArrayList<>();
                    for (Map.Entry<String, String> entry : entries.entrySet()) {
                        final long diff = DateZoneUtil.stringToDate_second(entry.getValue()).getTime() - System.currentTimeMillis();
                        if (diff <= providerProp.expired.toMillis()) {
                            remote.add(entry.getKey());
                        }
                    }
                    if (local.size() == remote.size()) {
                        remote.sort(String::compareTo);
                        boolean allMatch = true;
                        for (int i = 0; i < local.size(); i++) {
                            if (!local.get(i).equals(remote.get(i))) {
                                allMatch = false;
                                break;
                            }
                        }
                        if (!allMatch) {
                            remote.trimToSize();
                            typeToHosts.put(k, remote);
                        }
                    } else {
                        remote.sort(String::compareTo);
                        remote.trimToSize();
                        typeToHosts.put(k, remote);
                    }


                });
            }, l, l, TimeUnit.MILLISECONDS);
        }
    }
}
