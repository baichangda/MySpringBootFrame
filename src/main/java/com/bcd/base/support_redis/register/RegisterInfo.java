package com.bcd.base.support_redis.register;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.base.util.ExecutorUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class RegisterInfo {
    public final RegisterServer server;
    final ExecutorService pool = Executors.newSingleThreadExecutor();
    final BoundHashOperations<String, String, String> boundHashOperations;

    ArrayList<String> hosts;
    long lastUpdateTs;
    long index = 0;

    public RegisterInfo(RegisterServer server, RedisConnectionFactory redisConnectionFactory) {
        this.server = server;
        this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(RegisterUtil.redisKeyPre + server.name());
    }

    private ArrayList<String> hosts_unsafe() {
        long curTs = System.currentTimeMillis();
        long localExpireTs = curTs - server.consumer_localCacheExpired_ms;
        if (lastUpdateTs < localExpireTs) {
            //从redis中加载
            final Map<String, String> entries = boundHashOperations.entries();
            final ArrayList<String> temp;
            if (entries == null || entries.isEmpty()) {
                temp = new ArrayList<>();
            } else {
                temp = new ArrayList<>();
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    final String value = entry.getValue();
                    if (DateZoneUtil.stringToDate_second(value).getTime() >= server.consumer_providerInfoExpired_ms) {
                        temp.add(entry.getKey());
                    }
                }
                temp.sort(String::compareTo);
                temp.trimToSize();
            }
            this.hosts = temp;
            this.lastUpdateTs = curTs;
            return temp;
        } else {
            return hosts;
        }
    }

    private String host_unsafe() {
        ArrayList<String> list = hosts_unsafe();
        return list.get((int) (index++ % list.size()));
    }

    public ArrayList<String> hosts() {
        Future<ArrayList<String>> submit = pool.submit(this::hosts_unsafe);
        try {
            return submit.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    public String host() {
        Future<String> submit = pool.submit(this::host_unsafe);
        try {
            return submit.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    public void clearCache() {
        pool.execute(() -> {
            lastUpdateTs = 0;
            hosts = null;
        });
    }

    public void destroy() {
        ExecutorUtil.shutdownThenAwait(pool);
    }
}
