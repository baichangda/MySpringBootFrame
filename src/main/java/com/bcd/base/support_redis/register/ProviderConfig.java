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

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(value = "provider")
@EnableConfigurationProperties(ProviderProp.class)
@Component
public class ProviderConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    ProviderProp providerProp;

    ScheduledExecutorService providerPool;
    String[] types;
    BoundHashOperations<String, String, String>[] boundHashOperations;

    /**
     * 开启服务自动更新到redis
     *
     * @param providerProp
     * @param redisConnectionFactory
     */
    public void startProviderHeartbeat(ProviderProp providerProp, RedisConnectionFactory redisConnectionFactory) {
        if (providerProp.types.contains(",")) {
            types = providerProp.types.split(",");

        } else {
            types = new String[]{providerProp.types};
        }
        final RedisTemplate<String, String> stringStringRedisTemplate = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory);
        boundHashOperations = new BoundHashOperations[types.length];
        for (int i = 0; i < types.length; i++) {
            boundHashOperations[i] = stringStringRedisTemplate.boundHashOps(providerProp.redisKeyPre + types[i]);
        }
        providerPool = Executors.newSingleThreadScheduledExecutor();
        providerPool.scheduleAtFixedRate(() -> {
            final String s = DateZoneUtil.dateToString_second(new Date());
            for (BoundHashOperations<String, String, String> boundHashOperation : boundHashOperations) {
                boundHashOperation.put(providerProp.host, s);
            }
        }, 0, providerProp.reportPeriod.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!providerProp.host.isEmpty()) {
            startProviderHeartbeat(providerProp, redisConnectionFactory);
        }
    }
}
