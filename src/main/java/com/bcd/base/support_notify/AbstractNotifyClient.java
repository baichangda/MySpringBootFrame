package com.bcd.base.support_notify;

import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.ProducerFactory;
import com.bcd.base.support_kafka.ext.ProducerProp;
import com.bcd.base.support_kafka.ext.simple.SimpleKafkaConsumer;
import com.bcd.base.support_redis.RedisUtil;
import com.bcd.base.util.ExecutorUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("server.id")
@EnableConfigurationProperties(NotifyProp.class)
public abstract class AbstractNotifyClient extends SimpleKafkaConsumer {
    static Logger logger = LoggerFactory.getLogger(AbstractNotifyClient.class);
    public final NotifyProp notifyProp;
    private final Producer<String, byte[]> producer;
    public ScheduledExecutorService workPool;
    public final BoundHashOperations<String, String, String> boundHashOperations;
    public final Map<String, Map<String, ListenerInfo>> type_id_listenerInfo = new ConcurrentHashMap<>();
    private final String subscribeTopic;
    private final String notifyTopic;

    public AbstractNotifyClient(String type, RedisConnectionFactory redisConnectionFactory, NotifyProp notifyProp) {
        super("notifyClient(" + type + ")", new ConsumerProp(notifyProp.bootstrapServers, type + "_" + notifyProp.id), false, false, 100, 1, 100, true, 0, 0, "notify_" + type);
        this.subscribeTopic = "subscribe_" + type;
        this.notifyTopic = "notify_" + type;
        this.producer = ProducerFactory.newProducer(new ProducerProp(notifyProp.bootstrapServers));
        this.notifyProp = notifyProp;
        this.boundHashOperations = RedisUtil.newString_StringRedisTemplate(redisConnectionFactory).boundHashOps(this.notifyTopic);
    }


    public void init() {
        //开始消费
        super.init();
        workPool = Executors.newSingleThreadScheduledExecutor();
        workPool.scheduleWithFixedDelay(() -> {
            final long ts = System.currentTimeMillis();
            for (Map.Entry<String, Map<String, ListenerInfo>> entry1 : type_id_listenerInfo.entrySet()) {
                Map<String, ListenerInfo> value1 = entry1.getValue();
                Map<String, String> save = new HashMap<>();
                for (Map.Entry<String, ListenerInfo> entry2 : value1.entrySet()) {
                    ListenerInfo value2 = entry2.getValue();
                    value2.ts = ts;
                    save.put(entry2.getKey(), value2.toString());
                }
                boundHashOperations.putAll(save);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void destroy() {
        //停止消费
        super.destroy();
        //停止工作线程池
        ExecutorUtil.shutdownThenAwait(workPool);
        workPool = null;
    }

    /**
     * 订阅
     *
     * @param type
     * @param data
     */
    public String subscribe(String type, String data) {
        String id = RandomStringUtils.randomAlphanumeric(32);
        final ListenerInfo listenerInfo = new ListenerInfo(id, data, System.currentTimeMillis());
        type_id_listenerInfo.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(id, listenerInfo);
        //添加到redis
        boundHashOperations.put(id, listenerInfo.toString());
        //发送kafka通知
        producer.send(new ProducerRecord<>(subscribeTopic, ("1" + listenerInfo).getBytes()));
        logger.info("client subscribe type[{}] id[{}]", type, id);
        return id;
    }

    /**
     * 取消订阅
     *
     * @param type
     * @param id   {@link #subscribe(String, String)}返回的id
     */
    public void unSubscribe(String type, String id) {
        //删除缓存
        Optional.ofNullable(type_id_listenerInfo.get(type)).ifPresent(e -> e.remove(id));
        //从redis删除
        boundHashOperations.delete(id);
        //发送kafka通知
        producer.send(new ProducerRecord<>(subscribeTopic, ("2" + id).getBytes()));
        logger.info("client unSubscribe type[{}] id[{}]", type, id);
    }
}
