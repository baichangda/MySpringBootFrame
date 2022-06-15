package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 */
public abstract class AbstractConsumer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ThreadLocal<Consumer<String, byte[]>> consumers = new ThreadLocal<>();
    private final String[] topics;
    private final String groupId;
    private final ConsumerProp consumerProp;
    private final ThreadPoolExecutor consumerPool;
    private final ExecutorService workPool;
    public final AtomicInteger blockingNum;
    private final int maxBlockingNum;

    //是否自动释放阻塞、适用于工作内容为同步处理的逻辑
    private final boolean autoReduceBlockingNum;

    /**
     * @param consumerProp                 消费者属性
     * @param groupId                      消费组id
     * @param workThreadNum                工作线程个数
     * @param maxBlockingNum               最大阻塞
     * @param autoReleaseBlockingNum       在work完以后、自动释放blockingNum
     * @param topics                       消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            String groupId,
                            int workThreadNum,
                            int maxBlockingNum,
                            boolean autoReleaseBlockingNum,
                            String... topics) {
        this.consumerProp = consumerProp;
        this.groupId = groupId;
        this.consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        this.maxBlockingNum = maxBlockingNum;
        this.autoReduceBlockingNum = autoReleaseBlockingNum;
        this.topics = topics;
        this.blockingNum = new AtomicInteger(0);
        this.workPool = Executors.newFixedThreadPool(workThreadNum);

    }

    private Properties consumerProperties(ConsumerProp consumerProp, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProp.bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProp.enableAutoCommit);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, (int) consumerProp.autoCommitInterval.toMillis());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, (int) consumerProp.heartbeatInterval.toMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProp.autoOffsetReset);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, (int) consumerProp.sessionTimeout.toMillis());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) consumerProp.requestTimeout.toMillis());
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, consumerProp.maxPartitionFetchBytes);
        return props;
    }

    /**
     * 获取消费者
     *
     * @return
     */
    private Consumer<String, byte[]> getConsumer() {
        Consumer<String, byte[]> consumer = consumers.get();
        if (consumer == null) {
            consumer = new KafkaConsumer<>(consumerProperties(consumerProp, groupId));
            afterNewConsumer(consumer);
            consumers.set(consumer);
        }
        return consumer;
    }

    /**
     * 从所有topic的所有分区最新开始消费、忽略历史数据
     * <p>
     * 替换{@link #afterNewConsumer(Consumer)}中的内容
     *
     * @param consumer
     */
    protected final void consumeAllTopicPartitionsEnd(Consumer<String, byte[]> consumer) {
        logger.info("consumeAllTopicPartitionsEnd topics[{}]", Arrays.toString(topics));
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (String topic : topics) {
            topicPartitions.addAll(consumer.partitionsFor(topic).stream()
                    .map(item -> new TopicPartition(item.topic(), item.partition())).toList());
        }
        consumer.assign(topicPartitions);
        consumer.seekToEnd(topicPartitions);
        consumer.commitSync();
    }

    /**
     * 在构造消费者后、设置消费者
     *
     * @param consumer
     */
    protected void afterNewConsumer(Consumer<String, byte[]> consumer) {
        consumer.subscribe(Arrays.asList(topics));
    }

    /**
     * 用于在消费之后计数、主要用于性能统计
     *
     * @param count
     */
    protected void countAfterConsume(int count) {
        blockingNum.addAndGet(count);
    }

    /**
     * 消费
     *
     * @return
     */
    public ConsumerRecords<String, byte[]> consume() throws InterruptedException {
        if (blockingNum.get() >= maxBlockingNum) {
            Thread.sleep(500L);
            return null;
        }
        ConsumerRecords<String, byte[]> res = getConsumer().poll(Duration.ofSeconds(1));
        final int count;
        if (res != null && (count = res.count()) > 0) {
            countAfterConsume(count);
            return res;
        } else {
            return null;
        }
    }

    public abstract void onMessage(ConsumerRecord<String, byte[]> consumerRecord);

    /**
     * 初始化开启消费
     */
    public void init() {
        for (int i = 0; i < consumerPool.getMaximumPoolSize(); i++) {
            consumerPool.execute(() -> {
                while (true) {
                    try {
                        final ConsumerRecords<String, byte[]> consumerRecords = consume();
                        if (consumerRecords != null) {
                            workPool.execute(() -> {
                                try {
                                    for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                                        onMessage(consumerRecord);
                                    }
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                } finally {
                                    if (autoReduceBlockingNum) {
                                        blockingNum.addAndGet(-consumerRecords.count());
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error", e);
                        return;
                    }
                }
            });
        }
    }

}

