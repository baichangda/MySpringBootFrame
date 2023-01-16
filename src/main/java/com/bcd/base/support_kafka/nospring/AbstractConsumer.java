package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_disruptor.MyDisruptor;
import com.bcd.base.util.ExecutorUtil;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 */
public abstract class AbstractConsumer {
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
    private MyDisruptor<ConsumerRecord> myDisruptor;

    private volatile boolean running = true;

    /**
     * @param consumerProp   消费者属性
     * @param maxBlockingNum 最大阻塞数量、必需是2的倍数、如果不是向上取2的倍数
     * @param workThreadNum  工作线程个数
     * @param topics         消费的topic
     */
    public AbstractConsumer(Properties consumerProp,
                            int workThreadNum,
                            int maxBlockingNum,
                            String... topics) {
        this.consumerProp = consumerProp;
        this.workThreadNum = workThreadNum;
        this.maxBlockingNum = maxBlockingNum;
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
    }


    public void destroy() {
        running = false;
        ExecutorUtil.shutdownThenAwaitOneByOne(consumerExecutor);

        if (maxConsumeSpeed > 0) {
            ExecutorUtil.shutdownThenAwaitOneByOne(resetConsumeCountPool);
        }
        myDisruptor.destroy();
    }

    /**
     * @return
     */
    public AbstractConsumer autoReleaseBlocking() {
        this.autoReleaseBlocking = true;
        return this;
    }

    /**
     * @param maxConsumeSpeed 最大消费速度每秒(-1代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @return
     */
    public AbstractConsumer maxConsumeSpeed(int maxConsumeSpeed) {
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
        while (running) {
            try {
                //检查阻塞
                if (blockingNum.get() >= maxBlockingNum) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }
                //消费一批数据
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

                //发布消息
                for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                    //检查开始时间
                    myDisruptor.publish(consumerRecord);
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

class ConsumerRebalanceLogger implements ConsumerRebalanceListener {
    static final Logger logger = LoggerFactory.getLogger(ConsumerRebalanceLogger.class);

    final Consumer consumer;

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

