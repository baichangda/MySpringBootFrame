package com.bcd.base.support_redis.register;

import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.DateZoneUtil;
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

@ConditionalOnProperty(value = "register.host")
@EnableConfigurationProperties(RegisterProp.class)
@Component
public class RegisterUtil implements ApplicationListener<ContextRefreshedEvent>{
    final static String redisKeyPre="register:";
    final static ConcurrentHashMap<String, TypeInfo> typeToTypeInfo = new ConcurrentHashMap<>();
    /**
     * 最大感知超时时间
     * 即服务提供者、如果宕机、在此时间内会被消费者感知到
     * <p>
     * 此时间会产生如下两个重要时间
     * {@link #provider_heartbeat_s} 服务提供者心跳频率
     * {@link #consumer_localCacheExpired_ms} 消费者本地缓存过期时间
     * {@link #consumer_providerInfoExpired_ms} 消费者从redis获取信息、检查过期时间
     * <p>
     * 例如
     * maxTimeout_s=5s
     * provider_heartbeat_s=2s
     * consumer_localCacheExpired_ms=2000ms
     * consumer_providerInfoExpired_ms=3000ms
     * <p>
     * 极端场景下(最大过期信息场景)
     * 1、服务提供者发送完心跳立即宕机(从现在开始计算5s内消费者需要感知到)
     * 2、接着在3s内最后一刻、消费者获取到了服务提供者的信息
     * 逻辑分析:
     * 此时消费者本地缓存检查此信息在3s内、合法、并在本地缓存、缓存时间为2s、则在本地缓存失效前一刻、信息达到最大过期时间即接近5s
     * 此后本地缓存失效、再次从redis中获取并检查、剔除掉过期信息
     */
    final static int maxTimeout_s = 5;
    final static int provider_heartbeat_s;
    final static long consumer_localCacheExpired_ms;
    final static long consumer_providerInfoExpired_ms;

    static {
        int consumer_localCacheExpired = (maxTimeout_s - 1) / 2;
        consumer_localCacheExpired_ms = consumer_localCacheExpired * 1000L;
        provider_heartbeat_s = (maxTimeout_s - 1) - consumer_localCacheExpired;
        consumer_providerInfoExpired_ms = (provider_heartbeat_s + 1) * 1000L;
    }

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
        final String type;
        final BoundHashOperations<String, String, String> boundHashOperations;
        ProviderInfo providerInfo;
        final AtomicLong count = new AtomicLong();

        public TypeInfo(String type, RedisConnectionFactory redisConnectionFactory) {
            this.type = type;
            this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(redisKeyPre + type);
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
            long localExpireTs = curTs - consumer_localCacheExpired_ms;
            if (temp == null || temp.lastUpdateTs < localExpireTs) {
                synchronized (this) {
                    temp = providerInfo;
                    curTs = System.currentTimeMillis();
                    localExpireTs = curTs - consumer_localCacheExpired_ms;
                    if (temp == null || temp.lastUpdateTs < localExpireTs) {
                        //从redis中加载
                        final ArrayList<String> hosts = new ArrayList<>();
                        final Map<String, String> entries = boundHashOperations.entries();
                        if (!entries.isEmpty()) {
                            for (Map.Entry<String, String> entry : entries.entrySet()) {
                                final String value = entry.getValue();
                                if (DateZoneUtil.stringToDate_second(value).getTime() >= consumer_providerInfoExpired_ms) {
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
        final String[] types;
        if (registerProp.types.contains(",")) {
            types = registerProp.types.split(",");
        } else {
            types = new String[]{registerProp.types};
        }
        final RedisTemplate<String, String> stringStringRedisTemplate = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
        final BoundHashOperations<String, String, String>[] boundHashOperations = new BoundHashOperations[types.length];
        for (int i = 0; i < types.length; i++) {
            boundHashOperations[i] = stringStringRedisTemplate.boundHashOps(redisKeyPre + types[i]);
        }
        final ScheduledExecutorService providerPool = Executors.newSingleThreadScheduledExecutor();
        providerPool.scheduleAtFixedRate(() -> {
            final String host = registerProp.host;
            final String s = DateZoneUtil.dateToString_second(new Date());
            for (BoundHashOperations<String, String, String> boundHashOperation : boundHashOperations) {
                boundHashOperation.put(host, s);
            }
        }, 1, provider_heartbeat_s, TimeUnit.SECONDS);
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
        System.out.println((byte) a);
        System.out.println((byte) ((a + 1) & 0x7F));
        System.out.println((byte) ((a + 2) & 0x7F));
        System.out.println((byte) ((a + 3) & 0x7F));
        System.out.println(RegisterServer.test1);
    }
}

