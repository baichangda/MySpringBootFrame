package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class RegisterInfo {
    public final RegisterServer server;
    final BoundHashOperations<String, String, String> boundHashOperations;
    record Info(String[] hosts, long lastUpdateTs) {

    }

    volatile Info info;
    AtomicLong index = new AtomicLong(0);

    public RegisterInfo(RegisterServer server, RedisConnectionFactory redisConnectionFactory) {
        this.server = server;
        this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(RegisterUtil.redisKeyPre + server.name());
    }

    public String[] hosts() {
        Info temp = this.info;
        long curTs = System.currentTimeMillis();
        long localExpireTs = curTs - server.consumer_localCacheExpired_ms;
        if (temp == null || temp.lastUpdateTs < localExpireTs) {
            synchronized (this) {
                temp = this.info;
                curTs = System.currentTimeMillis();
                localExpireTs = curTs - server.consumer_localCacheExpired_ms;
                if (temp == null || temp.lastUpdateTs < localExpireTs) {
                    //从redis中加载
                    final Map<String, String> entries = boundHashOperations.entries();
                    final String[] hosts;
                    if (entries == null || entries.isEmpty()) {
                        hosts = new String[0];
                    } else {
                        hosts = new String[entries.size()];
                        int i = 0;
                        for (Map.Entry<String, String> entry : entries.entrySet()) {
                            final String value = entry.getValue();
                            if (DateZoneUtil.stringToDate_second(value).getTime() >= server.consumer_providerInfoExpired_ms) {
                                hosts[i++] = entry.getKey();
                            }
                        }
                        Arrays.sort(hosts);
                    }
                    this.info = new Info(hosts, curTs);
                    return hosts;
                } else {
                    return temp.hosts;
                }
            }
        } else {
            return temp.hosts;
        }
    }

    public String host() {
        String[] hosts = hosts();
        return hosts[(int) (index.getAndIncrement() % hosts.length)];
    }

    public void clearCache() {
        synchronized (this) {
            info = null;
        }
    }
}
