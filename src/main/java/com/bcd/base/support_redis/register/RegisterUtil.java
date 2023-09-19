package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * redis作为注册中心
 * 服务提供者启动后、向redis中插入自己的服务信息、并定时刷新时间戳表示存活
 * 消费者从redis中获取指定服务信息、然后检测其时间戳、判断是否存活、从存活的服务者轮询选取一个调用服务
 */
@ConditionalOnProperty(value = "register.host")
@EnableConfigurationProperties(RegisterProp.class)
@Component
public class RegisterUtil implements ApplicationListener<ContextRefreshedEvent> {

    static Logger logger = LoggerFactory.getLogger(RegisterUtil.class);

    final static String redisKeyPre = "register:";
    final static ConcurrentHashMap<RegisterServer, TypeInfo> typeToTypeInfo = new ConcurrentHashMap<>();

    static RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        RegisterUtil.redisConnectionFactory = redisConnectionFactory;
    }

    static RegisterProp registerProp;

    @Autowired
    public void setProviderProp(RegisterProp registerProp) {
        RegisterUtil.registerProp = registerProp;

    }

    record ProviderInfo(long lastUpdateTs, ArrayList<String> hosts) {
    }

    static class TypeInfo {
        final RegisterServer registerServer;
        final BoundHashOperations<String, String, String> boundHashOperations;
        ProviderInfo providerInfo;
        final AtomicLong count = new AtomicLong();

        public TypeInfo(RegisterServer registerServer, RedisConnectionFactory redisConnectionFactory) {
            this.registerServer = registerServer;
            this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(redisKeyPre + registerServer.name());
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
            long localExpireTs = curTs - registerServer.consumer_localCacheExpired_ms;
            if (temp == null || temp.lastUpdateTs < localExpireTs) {
                synchronized (this) {
                    temp = providerInfo;
                    curTs = System.currentTimeMillis();
                    localExpireTs = curTs - registerServer.consumer_localCacheExpired_ms;
                    if (temp == null || temp.lastUpdateTs < localExpireTs) {
                        //从redis中加载
                        final ArrayList<String> hosts = new ArrayList<>();
                        final Map<String, String> entries = boundHashOperations.entries();
                        if (!entries.isEmpty()) {
                            for (Map.Entry<String, String> entry : entries.entrySet()) {
                                final String value = entry.getValue();
                                if (DateZoneUtil.stringToDate_second(value).getTime() >= registerServer.consumer_providerInfoExpired_ms) {
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
     * 开启服务自动更新到redis
     *
     * @param registerProp
     * @param redisConnectionFactory
     */
    @SuppressWarnings("unchecked")
    public void startProviderHeartbeat(RegisterProp registerProp, RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, String> stringStringRedisTemplate = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
        final ScheduledExecutorService providerPool = Executors.newSingleThreadScheduledExecutor();
        for (RegisterServer registerServer : registerProp.servers) {
            final BoundHashOperations<String, String, String> boundHashOperation = stringStringRedisTemplate.boundHashOps(redisKeyPre + registerServer.name());
            final String host = registerProp.host;
            providerPool.scheduleAtFixedRate(() -> boundHashOperation.put(host, DateZoneUtil.dateToString_second(new Date())), 1, registerServer.provider_heartbeat_s, TimeUnit.SECONDS);
        }
        //添加关机钩子
        Runtime.getRuntime().addShutdownHook(new Thread(providerPool::shutdown));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!registerProp.host.isEmpty()) {
            startProviderHeartbeat(registerProp, redisConnectionFactory);
        }
    }


    /**
     * 根据访问序号轮流使用host
     *
     * @param registerServer
     * @return
     */
    public static String host(RegisterServer registerServer) {
        final TypeInfo typeInfo = typeToTypeInfo.computeIfAbsent(registerServer, k -> new TypeInfo(k, redisConnectionFactory));
        return typeInfo.host();
    }

    /**
     * 获取可用host
     *
     * @param registerServer
     * @return
     */
    public static ArrayList<String> hosts(RegisterServer registerServer) {
        final TypeInfo typeInfo = typeToTypeInfo.computeIfAbsent(registerServer, k -> new TypeInfo(k, redisConnectionFactory));
        return typeInfo.hosts();
    }

    public static void main(String[] args) {
        System.out.println(RegisterServer.test1);
        System.out.println(RegisterServer.test1.name());
        System.out.println(RegisterServer.valueOf("test2"));
    }
}

