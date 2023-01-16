package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_disruptor.MyDisruptor;
import com.bcd.base.util.ExecutorUtil;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 * <p>
 * 用于消费指定时间段的数据
 * 注意
 * <p>
 * 只能有一个消费者、因为是手动分区{@link Consumer#assign(Collection)}不会进行负载均衡、区别于{@link Consumer#subscribe(Pattern)}是交给kafka自动分区
 * <p>
 * 要求kafka版本大于 0.10.0、因为依赖{@link Consumer#offsetsForTimes(Map)}
 * <p>
 * 会在消费完毕后自动停止销毁、即所有分区消费完毕后销毁
 */
public abstract class AbstractConsumerForTimeRange {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Consumer<String, byte[]> consumer;
    private final String[] topics;
    private final Properties consumerProp;
    /**
     * 消费线程池、默认一个
     */
    private ExecutorService consumerExecutor;
    /**
     * 工作线程数量
     */
    private final int workThreadNum;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    private final int maxBlockingNum;
    /**
     * 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     */
    private boolean autoReleaseBlocking;
    /**
     * 当前阻塞数量
     */
    public final AtomicInteger blockingNum = new AtomicInteger();

    /**
     * 最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     */
    private int maxConsumeSpeed;
    private ScheduledExecutorService resetConsumeCountPool;
    private AtomicInteger consumeCount;

    private final long startTimeTs;
    private final long endTimeTs;


    private MyDisruptor<ConsumerRecord> myDisruptor;


    /**
     * @param consumerProp   消费者属性
     * @param maxBlockingNum 最大阻塞数量、必需是2的倍数、如果不是向上取2的倍数
     * @param workThreadNum  工作线程个数
     * @param startTimeTs    获取数据开始时间戳
     * @param endTimeTs      获取数据结束时间戳
     * @param topics         消费的topic
     */
    public AbstractConsumerForTimeRange(Properties consumerProp,
                                        int workThreadNum,
                                        int maxBlockingNum,
                                        long startTimeTs,
                                        long endTimeTs,
                                        String... topics) {
        this.consumerProp = consumerProp;
        this.workThreadNum = workThreadNum;
        this.maxBlockingNum = maxBlockingNum;
        this.startTimeTs = startTimeTs;
        this.endTimeTs = endTimeTs;
        this.topics = topics;
    }


    public void init() {
        //初始化消费者
        consumer = new KafkaConsumer<>(consumerProp);
        afterNewConsumer(consumer);

        //初始化消费线程
        this.consumerExecutor = Executors.newSingleThreadExecutor();

        if (maxConsumeSpeed > 0) {
            consumeCount = new AtomicInteger();
            resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor();
        }

        //初始化disruptor
        java.util.function.Consumer[] handlers = new java.util.function.Consumer[workThreadNum];
        for (int i = 0; i < workThreadNum; i++) {
            handlers[i] = (java.util.function.Consumer<ConsumerRecord<String, byte[]>>) this::onMessageInternal;
        }
        myDisruptor = new MyDisruptor<>(maxBlockingNum, ProducerType.SINGLE, new BlockingWaitStrategy());
        myDisruptor.handle(handlers);

        //启动
        myDisruptor.init();
        consumerExecutor.execute(this::consume);
        if (maxConsumeSpeed > 0) {
            resetConsumeCountPool.scheduleAtFixedRate(() -> consumeCount.set(0), 1, 1, TimeUnit.SECONDS);
        }

        //销毁消费者、因为消费者执行的是死循环、只有当满足条件时候才会退出
        consumerExecutor.shutdown();
    }


    public void destroyByConsumerExecutor() {
        if (maxConsumeSpeed > 0) {
            ExecutorUtil.shutdownThenAwaitOneByOne(resetConsumeCountPool);
        }
        //disruptor
        myDisruptor.destroy();
    }

    /**
     * @return
     */
    public AbstractConsumerForTimeRange autoReleaseBlocking() {
        this.autoReleaseBlocking = true;
        return this;
    }

    /**
     * @param maxConsumeSpeed 最大消费速度每秒(-1代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @return
     */
    public AbstractConsumerForTimeRange maxConsumeSpeed(int maxConsumeSpeed) {
        this.maxConsumeSpeed = maxConsumeSpeed;
        return this;
    }

    private Properties consumerProperties(ConsumerProp consumerProp) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProp.groupId);
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
     * 设置偏移量、同时分配分区
     *
     * @param consumer
     */
    protected void afterNewConsumer(Consumer<String, byte[]> consumer) {
        //获取所有的分区
        final Map<TopicPartition, Long> map = Arrays.stream(topics)
                .map(consumer::partitionsFor)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(e -> new TopicPartition(e.topic(), e.partition()), e -> startTimeTs));

        //获取偏移量
        final Map<TopicPartition, OffsetAndTimestamp> startOffsetMap = consumer.offsetsForTimes(map);

        //获取偏移量
        final Map<TopicPartition, Long> seekMap = new HashMap<>();
        startOffsetMap.forEach((k, v) -> {
            logger.info("consumer fetch [{}:{}] offset[{}]", k.topic(), k.partition(), v == null ? null : v.offset());
            //null说明找不到message、导致这个原因可能是kafka版本过低、这种情况不进行seek
            if (v != null) {
                seekMap.put(k, v.offset());
            }
        });

        //分配
        consumer.assign(map.keySet());

        seekMap.forEach((k, v) -> {
            logger.info("consumer seek [{}:{}] offset[{}]", k.topic(), k.partition(), v);
            consumer.seek(k, v);
        });
    }

    /**
     * 用于在消费之后计数、主要用于性能统计
     *
     * @param count
     */
    protected void countAfterConsume(int count) {

    }

    private void checkSpeedAndSleep(int count) throws InterruptedException {
        //检查速度、如果速度太快则阻塞
        if (maxConsumeSpeed > 0) {
            //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
            final int curConsumeCount = consumeCount.addAndGet(count);
            if (curConsumeCount >= maxConsumeSpeed) {
                do {
                    TimeUnit.MILLISECONDS.sleep(10);
                } while (consumeCount.get() >= maxConsumeSpeed);
            }
        }
    }

    /**
     * 消费
     *
     * @return
     */
    public void consume() {
        while (true) {
            try {
                //检查阻塞
                if (blockingNum.get() >= maxBlockingNum) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }
                //获取消费者
                ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(60));

                if (consumerRecords == null || consumerRecords.isEmpty()) {
                    continue;
                }

                //统计
                final int count = consumerRecords.count();
                blockingNum.addAndGet(count);

                countAfterConsume(count);

                //检查速度、如果速度太快则阻塞
                checkSpeedAndSleep(count);

                final Set<TopicPartition> removeSet = new HashSet<>();

                //发布消息
                for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                    //检查开始时间
                    final long ts;
                    if (consumerRecord.timestampType() != TimestampType.NO_TIMESTAMP_TYPE &&
                            (ts = consumerRecord.timestamp()) != ConsumerRecord.NO_TIMESTAMP) {

                        if (ts < startTimeTs) {
                            blockingNum.decrementAndGet();
                            continue;
                        }
                        //检查结束时间
                        if (ts > endTimeTs) {
                            blockingNum.decrementAndGet();
                            //记录需要移除的节点
                            removeSet.add(new TopicPartition(consumerRecord.topic(), consumerRecord.partition()));
                            continue;
                        }
                    }
                    myDisruptor.publish(consumerRecord);
                }

                //重新设置订阅
                if (!removeSet.isEmpty()) {
                    final String reduce = removeSet.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + "," + e2).get();
                    logger.info("consumer assignment change, remove [{}]", reduce);
                    final Set<TopicPartition> assignment = consumer.assignment();
                    for (TopicPartition remove : removeSet) {
                        assignment.remove(remove);
                    }
                    consumer.assign(assignment);
                    //如果订阅为空、退出消费
                    if (assignment.isEmpty()) {
                        logger.info("consumer assignment empty, exit");
                        break;
                    }
                }
            } catch (Exception ex) {
                logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error", ex);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }

        //退出时候销毁其他资源
        destroyByConsumerExecutor();
    }


    public abstract void onMessage(ConsumerRecord<String, byte[]> consumerRecord);

    private void onMessageInternal(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
            onMessage(consumerRecord);
        } catch (Exception ex) {
            logger.error("onMessageInternal error", ex);
        }
        if (autoReleaseBlocking) {
            blockingNum.decrementAndGet();
        }
    }
}

