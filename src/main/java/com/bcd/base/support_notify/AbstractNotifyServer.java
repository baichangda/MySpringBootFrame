package com.bcd.base.support_notify;

import com.bcd.base.support_kafka.nospring.AbstractConsumer;
import com.bcd.base.support_kafka.nospring.ConsumerProp;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.ExecutorUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("server.id")
@EnableConfigurationProperties(NotifyProp.class)
public abstract class AbstractNotifyServer extends AbstractConsumer {
    static Logger logger = LoggerFactory.getLogger(AbstractNotifyServer.class);
    public final String type;
    public final BoundHashOperations<String, String, String> boundHashOperations;
    public final NotifyProp notifyProp;
    public ScheduledExecutorService workPool;
    public final KafkaTemplate<byte[], byte[]> kafkaTemplate;
    private final String subscribeTopic;
    private final String notifyTopic;

    public AbstractNotifyServer(String type, RedisConnectionFactory redisConnectionFactory, KafkaTemplate<byte[], byte[]> kafkaTemplate, NotifyProp notifyProp) {
        super(new ConsumerProp(), 1, 1, 0, true, "subscribe_" + type);
        this.subscribeTopic = "subscribeTopic_" + type;
        this.notifyTopic = "notify_" + type;
        this.type = type;
        this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(this.notifyTopic);
        this.kafkaTemplate = kafkaTemplate;
        this.notifyProp = notifyProp;
    }


    public void init() {
        //开始消费
        super.init();
        workPool = Executors.newSingleThreadScheduledExecutor();
        startUpdateCacheFromRedis();
    }

    public void destroy() {
        //停止消费
        super.destroy();
        //停止工作线程池
        ExecutorUtil.shutdownAllThenAwaitAll(workPool);
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
                workPool.execute(() -> onListenerInfoAdd(listenerInfo));
                logger.info("server subscribe type[{}] id[{}]", type, listenerInfo.id);
            } catch (IOException e) {
                logger.error("ListenerInfo.fromString error type[{}] value:\n{}", type, value);
            }
        } else {
            workPool.execute(() -> onListenerInfoRemove(new String[]{content}));
            logger.info("server unsubscribe type[{}] id[{}]", type, content);
        }
    }

    /**
     * 每隔1min
     * 从redis中获取所有的监听信息、更新本地缓存
     */
    private void startUpdateCacheFromRedis() {
        workPool.scheduleWithFixedDelay(() -> {
            //过滤超过60s的监听、视为已经失效、移除掉
            Map<String, String> entries = boundHashOperations.entries();
            if (entries != null) {
                final List<String> keyList = new ArrayList<>();
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    String value = entry.getValue();
                    try {
                        final ListenerInfo listenerInfo = ListenerInfo.fromString(value);
                        if ((System.currentTimeMillis() - listenerInfo.ts) > 60000) {
                            keyList.add(entry.getKey());
                        }
                    } catch (IOException e) {
                        logger.error("ListenerInfo.fromString error type[{}] value:\n{}", type, value);
                    }
                }
                if (!keyList.isEmpty()) {
                    boundHashOperations.delete(keyList.toArray(new Object[0]));
                    workPool.execute(() -> onListenerInfoRemove(keyList.toArray(new String[0])));
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public abstract void onListenerInfoAdd(ListenerInfo listenerInfo);

    public abstract void onListenerInfoRemove(String[] id);

    public void sendNotify(final byte[] bytes) {
        kafkaTemplate.send(notifyTopic, bytes);
    }

}
