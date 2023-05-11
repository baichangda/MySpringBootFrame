package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProviderUtil {
    static RedisConnectionFactory redisConnectionFactory;

    static ProviderProp providerProp;

    @Autowired
    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        ProviderUtil.redisConnectionFactory = redisConnectionFactory;
    }

    final static ConcurrentHashMap<String, TypeInfo> typeToTypeInfo = new ConcurrentHashMap<>();

    static class ProviderInfo {
        final long lastUpdateTs;
        final ArrayList<String> hosts;

        public ProviderInfo(long lastUpdateTs, ArrayList<String> hosts) {
            this.lastUpdateTs = lastUpdateTs;
            this.hosts = hosts;
        }
    }

    static class TypeInfo {
        final String type;
        final BoundHashOperations<String, String, String> boundHashOperations;
        volatile ProviderInfo providerInfo;
        final AtomicLong count = new AtomicLong();

        public TypeInfo(String type, RedisConnectionFactory redisConnectionFactory) {
            this.type = type;
            this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(providerProp.redisKeyPre + type);
        }

        public String host() {
            final ArrayList<String> hosts = hosts();
            if (hosts.isEmpty()) {
                return null;
            }
            return hosts.get((int) (count.getAndIncrement() % hosts.size()));
        }

        public ArrayList<String> hosts() {
            ProviderInfo temp = providerInfo;
            long curTs = System.currentTimeMillis();
            long expireTs = curTs - providerProp.expired.toMillis();
            if (temp == null || temp.lastUpdateTs < expireTs) {
                synchronized (this) {
                    temp = providerInfo;
                    curTs = System.currentTimeMillis();
                    expireTs = curTs - providerProp.expired.toMillis();
                    if (temp == null || temp.lastUpdateTs < expireTs) {
                        //从redis中加载
                        final ArrayList<String> hosts = new ArrayList<>();
                        final Map<String, String> entries = boundHashOperations.entries();
                        if (!entries.isEmpty()) {
                            for (Map.Entry<String, String> entry : entries.entrySet()) {
                                final String value = entry.getValue();
                                if (DateZoneUtil.stringToDate_second(value).getTime() >= expireTs) {
                                    hosts.add(entry.getKey());
                                }
                            }
                            hosts.sort(String::compareTo);
                            hosts.trimToSize();
                            temp = new ProviderInfo(curTs, hosts);
                        } else {
                            temp = new ProviderInfo(curTs, new ArrayList<>());
                        }
                        this.providerInfo = temp;
                    }
                }
            }
            return temp.hosts;
        }
    }

    /**
     * 根据访问序号轮流使用host
     * @param type
     * @return
     */
    public static String host(String type) {
        final TypeInfo typeInfo = typeToTypeInfo.computeIfAbsent(type, k -> new TypeInfo(k, redisConnectionFactory));
        return typeInfo.host();
    }

    /**
     * 获取可用host
     *
     * @param type
     * @return
     */
    public static ArrayList<String> hosts(String type) {
        final TypeInfo typeInfo = typeToTypeInfo.computeIfAbsent(type, k -> new TypeInfo(k, redisConnectionFactory));
        return typeInfo.hosts();
    }

    public static void main(String[] args) {
        byte a = Byte.MAX_VALUE;
        System.out.println((byte) (a + 2));
        System.out.println(-127 % 2);
    }
}

