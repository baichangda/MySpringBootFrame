package com.bcd.base.support_notify;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.ProducerFactory;
import com.bcd.base.support_kafka.ext.ProducerProp;
import com.bcd.base.support_kafka.ext.simple.ThreadDrivenKafkaConsumer;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.ExecutorUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 由消息提供方实现、并注册为spring bean
 * 主要实现
 * {@link #onListenerInfoUpdate (ListenerInfo)} 用于更新订阅
 * 逻辑流程
 * 1、启动时候会从redis中加载所有订阅信息
 * 2、kafka监听订阅和取消订阅请求
 * 3、每隔1min检查订阅信息是否有变化
 */
@ConditionalOnProperty("server.id")
@EnableConfigurationProperties(NotifyProp.class)
public abstract class AbstractNotifyServer extends ThreadDrivenKafkaConsumer {
    static Logger logger = LoggerFactory.getLogger(AbstractNotifyServer.class);
    public final String type;
    public final BoundHashOperations<String, String, String> boundHashOperations;
    public final NotifyProp notifyProp;
    public ScheduledExecutorService workPool;
    private final Producer<String, byte[]> producer;
    private final String subscribeTopic;
    private final String notifyTopic;

    private Map<String, ListenerInfo> cache = new HashMap<>();

    public AbstractNotifyServer(String type, RedisConnectionFactory redisConnectionFactory, NotifyProp notifyProp) {
        super("notifyServer(" + type + ")", new ConsumerProp(notifyProp.bootstrapServers, type + "_" + notifyProp.id), false, false, 100, 1, 100, true, 0, 0, "subscribe_" + type);
        this.subscribeTopic = "subscribe_" + type;
        this.notifyTopic = "notify_" + type;
        this.type = type;
        this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(this.notifyTopic);
        this.producer = ProducerFactory.newProducer(new ProducerProp(notifyProp.bootstrapServers));
        this.notifyProp = notifyProp;
    }


    public void init() {
        workPool = Executors.newSingleThreadScheduledExecutor();
        //初始化redis订阅数据到缓存
        Future<?> future = checkAndUpdateCache();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw BaseRuntimeException.getException(e);
        }
        //开始消费
        super.init();
        startUpdateCacheFromRedis();
    }

    public void destroy() {
        //停止消费
        super.destroy();
        //停止工作线程池
        ExecutorUtil.shutdownThenAwait(workPool);
        workPool = null;
    }

    @Override
    public void onMessage(ConsumerRecord<String, byte[]> consumerRecord) {
        final byte[] value = consumerRecord.value();
        final char type = (char) value[0];
        final String content = new String(value, 1, value.length - 1);
        if (type == '1') {
            try {
                final ListenerInfo listenerInfo = ListenerInfo.fromString(content);
                workPool.execute(() -> {
                    cache.put(listenerInfo.id, listenerInfo);
                    onListenerInfoUpdate(cache.values().toArray(new ListenerInfo[0]));
                });
                logger.info("server subscribe type[{}] id[{}]", type, listenerInfo.id);
            } catch (IOException e) {
                logger.error("ListenerInfo.fromString error type[{}] value:\n{}", type, value);
            }
        } else {
            workPool.execute(() -> {
                cache.remove(content);
                onListenerInfoUpdate(cache.values().toArray(new ListenerInfo[0]));
            });
            logger.info("server unsubscribe type[{}] id[{}]", type, content);
        }
    }

    /**
     * 每隔1min
     * 从redis中获取所有的监听信息、更新本地缓存
     */
    private void startUpdateCacheFromRedis() {
        workPool.scheduleWithFixedDelay(this::checkAndUpdateCache, 1, 1, TimeUnit.MINUTES);
    }


    private Future<?> checkAndUpdateCache() {
        Map<String, ListenerInfo> aliveMap = new HashMap<>();
        Map<String, String> entries = boundHashOperations.entries();
        if (entries != null) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                String value = entry.getValue();
                try {
                    final ListenerInfo listenerInfo = ListenerInfo.fromString(value);
                    if ((System.currentTimeMillis() - listenerInfo.ts) <= 60000) {
                        aliveMap.put(entry.getKey(), listenerInfo);
                    }
                } catch (IOException e) {
                    logger.error("ListenerInfo.fromString error type[{}] value:\n{}", type, value);
                }
            }
        }
        return workPool.submit(() -> {
            boolean update;
            if (aliveMap.size() == cache.size()) {
                update = aliveMap.entrySet().stream().anyMatch(e -> !cache.containsKey(e.getKey()));
            } else {
                update = true;
            }
            if (update) {
                cache = aliveMap;
                onListenerInfoUpdate(cache.values().toArray(new ListenerInfo[0]));
            }
        });
    }

    /**
     * 注意不要阻塞此方法
     * 因为针对该类所有操作都是由单线程完成
     *
     * @param listenerInfos 全量有效的订阅信息
     */
    public abstract void onListenerInfoUpdate(ListenerInfo[] listenerInfos);

    public void sendNotify(final byte[] bytes) {
        producer.send(new ProducerRecord<>(notifyTopic, bytes));
    }

}
