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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    public final static String redisKeyPre = "register:";

    final static ConcurrentHashMap<RegisterServer, RegisterInfo> registerServer_registerInfo = new ConcurrentHashMap<>();

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
     * @param server
     * @return
     */
    public static String host(RegisterServer server) {
        return registerServer_registerInfo.computeIfAbsent(server, k -> new RegisterInfo(k, redisConnectionFactory)).host();
    }

    /**
     * 获取可用host
     *
     * @param server
     * @return
     */
    public static ArrayList<String> hosts(RegisterServer server) {
        return registerServer_registerInfo.computeIfAbsent(server, k -> new RegisterInfo(k, redisConnectionFactory)).hosts();
    }

    /**
     * 清除本地缓存
     * 下一次会从redis中获取最新数据
     * @param server
     */
    public static void clearCache(RegisterServer server) {
        registerServer_registerInfo.computeIfAbsent(server, k -> new RegisterInfo(k, redisConnectionFactory)).clearCache();
    }

    public static void main(String[] args) {
        System.out.println(RegisterServer.test1);
        System.out.println(RegisterServer.test1.name());
        System.out.println(RegisterServer.valueOf("test2"));
    }
}

