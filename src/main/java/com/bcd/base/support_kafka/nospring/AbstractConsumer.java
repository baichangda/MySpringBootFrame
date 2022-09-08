package com.bcd.base.support_kafka.nospring;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private final int maxConsumeSpeed;

    private final ScheduledExecutorService resetConsumeCountPool;

    private final AtomicInteger consumeCount;

    /**
     * @param consumerProp           消费者属性
     * @param groupId                消费组id
     * @param workThreadNum          工作线程个数
     * @param maxBlockingNum         最大阻塞
     * @param maxConsumeSpeed        最大消费速度每秒(-1代表不限制)、不能设置的过小、因为每次从kafka消费是一批数据、如果设置过小、可能会导致阻塞死
     *                               至少大于一批数据大小
     * @param autoReleaseBlockingNum 在work完以后、自动释放blockingNum
     * @param topics                 消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            String groupId,
                            int workThreadNum,
                            int maxBlockingNum,
                            int maxConsumeSpeed,
                            boolean autoReleaseBlockingNum,
                            String... topics) {
        this.consumerProp = consumerProp;
        this.groupId = groupId;
        this.consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        this.maxBlockingNum = maxBlockingNum;
        this.maxConsumeSpeed = maxConsumeSpeed;
        if (maxConsumeSpeed > 0) {
            consumeCount = new AtomicInteger();
            resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor();
        } else {
            consumeCount = null;
            resetConsumeCountPool = null;
        }
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
                    .map(item -> new TopicPartition(item.topic(), item.partition())).collect(Collectors.toList()));
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
        consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceLogger(consumer));
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
        //检查阻塞
        if (blockingNum.get() >= maxBlockingNum) {
            TimeUnit.MILLISECONDS.sleep(500);
            return null;
        }

        ConsumerRecords<String, byte[]> res = getConsumer().poll(Duration.ofSeconds(1));
        final int count;
        if (res != null && (count = res.count()) > 0) {
            //消费成功后统计计数
            countAfterConsume(count);
            if (maxConsumeSpeed > 0) {
                //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
                final int curConsumeCount = consumeCount.addAndGet(count);
                if (curConsumeCount < maxConsumeSpeed) {
                    return res;
                } else {
                    while (true) {
                        TimeUnit.MILLISECONDS.sleep(50);
                        if (consumeCount.get() < maxConsumeSpeed) {
                            break;
                        }
                    }
                    return res;
                }
            } else {
                return res;
            }
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

        if (maxConsumeSpeed > 0) {
            resetConsumeCountPool.scheduleAtFixedRate(() -> consumeCount.set(0), 1, 1, TimeUnit.SECONDS);
        }
    }
}

class ConsumerRebalanceLogger implements ConsumerRebalanceListener {
    static Logger logger = LoggerFactory.getLogger(ConsumerRebalanceLogger.class);

    Consumer consumer;

    public ConsumerRebalanceLogger(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        String msg = partitions.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + ";" + e2).orElse("");
        logger.info("consumer[{}] onPartitionsRevoked [{}]", consumer.toString(), msg);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        String msg = partitions.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + ";" + e2).orElse("");
        logger.info("consumer[{}] onPartitionsAssigned [{}]", consumer.toString(), msg);
    }
}

