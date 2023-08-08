package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExecutorUtil;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 */
public abstract class AbstractConsumer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 工作线程数量
     */
    private final int workThreadNum;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    private final int maxBlockingNum;

    /**
     * 当前阻塞数量
     */
    public final AtomicInteger blockingNum = new AtomicInteger();

    /**
     * 消费topic
     */
    private final String[] topics;

    /**
     * kafka消费者
     */
    private final Consumer<String, byte[]> consumer;
    /**
     * 消费线程
     */
    private final Thread consumeThread;
    /**
     * 工作线程数组
     */
    private final Thread[] workThreads;
    /**
     * 工作线程队列
     */
    private final ArrayBlockingQueue<ConsumerRecord<String, byte[]>>[] queues;
    private final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue;

    /**
     * 当前消费者是否可用
     */
    private volatile boolean available;

    /**
     * 最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     */
    private final int maxConsumeSpeed;
    private final AtomicInteger consumeCount;
    private ScheduledExecutorService resetConsumeCountPool;

    /**
     * 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     */
    private final boolean autoReleaseBlocking;

    /**
     * 控制退出线程标志
     */
    private volatile boolean running_consume = true;
    private volatile boolean running_work = true;

    private final boolean workThreadPerQueue;


    /**
     * @param consumerProp        消费者属性
     * @param maxBlockingNum      最大阻塞数量
     * @param workThreadNum       工作线程个数
     * @param workThreadPerQueue  是否每一个工作线程都有一个队列
     *                            true时候、可以通过{@link #index(ConsumerRecord)}实现记录关联work线程、这在某些场景可以避免线程竞争
     *                            false时候、共享一个队列
     * @param autoReleaseBlocking 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param maxConsumeSpeed     最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @param topics              消费的topic
     */
    public AbstractConsumer(ConsumerProp consumerProp,
                            int workThreadNum,
                            int maxBlockingNum,
                            boolean workThreadPerQueue,
                            boolean autoReleaseBlocking,
                            int maxConsumeSpeed,
                            String... topics) {
        this.workThreadNum = workThreadNum;
        this.maxBlockingNum = maxBlockingNum;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.workThreadPerQueue = workThreadPerQueue;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.topics = topics;

        this.consumer = new KafkaConsumer<>(this.consumerProperties(consumerProp));

        //初始化消费线程
        this.consumeThread = new Thread(this::consume);

        //初始化工作线程
        this.workThreads = new Thread[workThreadNum];

        //根据是否公用一个队列、来指定构造
        if (workThreadPerQueue) {
            this.queues = null;
            this.queue = new ArrayBlockingQueue<>(maxBlockingNum);
            for (int i = 0; i < workThreadNum; i++) {
                workThreads[i] = new Thread(() -> work(this.queue));
            }
        } else {
            this.queue = null;
            this.queues = new ArrayBlockingQueue[workThreadNum];
            for (int i = 0; i < workThreadNum; i++) {
                final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue = new ArrayBlockingQueue<>(maxBlockingNum);
                this.queues[i] = queue;
                workThreads[i] = new Thread(() -> work(queue));
            }
        }

        if (maxConsumeSpeed > 0) {
            this.consumeCount = new AtomicInteger();
        } else {
            this.consumeCount = null;
        }
    }

    public void init() {
        if (!available) {
            synchronized (this) {
                if (!available) {
                    //订阅topic
                    consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceLogger(consumer));

                    //初始化重置消费计数线程池(如果有限制最大消费速度)、提交工作任务、每秒重置消费数量
                    if (maxConsumeSpeed > 0) {
                        resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor();
                        resetConsumeCountPool.scheduleAtFixedRate(() -> {
                            consumeCount.getAndSet(0);
                        }, 1, 1, TimeUnit.SECONDS);
                    }

                    //初始化工作线程池、提交工作任务
                    for (int i = 0; i < workThreadNum; i++) {
                        workThreads[i].start();
                    }

                    //初始化消费线程、提交消费任务
                    consumeThread.start();

                    //增加销毁回调
                    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));

                    //标记可用
                    available = true;
                }
            }
        }
    }


    public void destroy() {
        if (!available) {
            synchronized (this) {
                if (!available) {
                    try {
                        //打上退出标记、等待消费线程退出
                        running_consume = false;
                        while (consumeThread.isAlive()) {
                            TimeUnit.MILLISECONDS.sleep(100L);
                        }
                        //销毁重置计数线程池(如果存在)
                        if (resetConsumeCountPool != null) {
                            ExecutorUtil.shutdownAllThenAwaitAll(resetConsumeCountPool);
                        }

                        //等待队列中为空、然后停止工作线程池、避免出现数据丢失
                        for (ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue : queues) {
                            while (!queue.isEmpty()) {
                                TimeUnit.MILLISECONDS.sleep(100L);
                            }
                        }
                        //打上退出标记、等待工作线程退出
                        running_work = false;
                        for (Thread workThread : workThreads) {
                            while (workThread.isAlive()) {
                                TimeUnit.MILLISECONDS.sleep(100L);
                            }
                        }

                        //标记不可用
                        available = false;
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                }
            }
        }
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
        props.putAll(consumerProp.properties);
        return props;
    }


    /**
     * 用于在消费之后计数、主要用于性能统计
     *
     * @param count
     */
    protected void countAfterConsume(int count) {

    }

    long no = 0;

    /**
     * 当{@link #workThreadPerQueue}为true时候生效
     * 可以根据数据内容绑定到对应的工作线程上、避免线程竞争
     *
     * @param consumerRecord
     * @return [0-{@link #workThreadNum})中某个值
     */
    protected int index(ConsumerRecord<String, byte[]> consumerRecord) {
        if (consumerRecord.key() == null) {
            return (int) ((no++) % workThreadNum);
        } else {
            return consumerRecord.key().hashCode() % workThreadNum;
        }
    }

    /**
     * 消费
     */
    public void consume() {
        if (workThreadPerQueue) {
            while (running_consume) {
                try {
                    //检查阻塞
                    if (blockingNum.get() >= maxBlockingNum) {
                        TimeUnit.MILLISECONDS.sleep(100);
                        continue;
                    }
                    //消费一批数据
                    final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(60));

                    if (consumerRecords == null || consumerRecords.isEmpty()) {
                        continue;
                    }

                    //统计
                    final int count = consumerRecords.count();
                    blockingNum.addAndGet(count);
                    countAfterConsume(count);

                    //检查速度、如果速度太快则阻塞
                    if (maxConsumeSpeed > 0) {
                        //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
                        final int curConsumeCount = consumeCount.addAndGet(count);
                        if (curConsumeCount >= maxConsumeSpeed) {
                            do {
                                TimeUnit.MILLISECONDS.sleep(50);
                            } while (consumeCount.get() >= maxConsumeSpeed);
                        }
                    }
                    //发布消息
                    for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                        //放入队列
                        queues[index(consumerRecord)].put(consumerRecord);
                    }
                } catch (Exception ex) {
                    logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error,Try Again After 3s", ex);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                }
            }
        } else {
            while (running_consume) {
                try {
                    //检查阻塞
                    if (blockingNum.get() >= maxBlockingNum) {
                        TimeUnit.MILLISECONDS.sleep(100);
                        continue;
                    }
                    //消费一批数据
                    final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(60));

                    if (consumerRecords == null || consumerRecords.isEmpty()) {
                        continue;
                    }

                    //统计
                    final int count = consumerRecords.count();
                    blockingNum.addAndGet(count);
                    countAfterConsume(count);

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
                    //发布消息
                    for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
                        //放入队列
                        queue.put(consumerRecord);
                    }
                } catch (Exception ex) {
                    logger.error("Kafka Consumer[" + Arrays.stream(topics).reduce((e1, e2) -> e1 + "," + e2) + "] Cycle Error,Try Again After 3s", ex);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        throw BaseRuntimeException.getException(e);
                    }
                }
            }
        }

        logger.info("consumer[{}] exit", this.getClass().getName());
    }

    /**
     * 工作线程
     *
     * @param queue
     */
    private void work(final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue) {
        try {
            while (running_work) {
                final ConsumerRecord<String, byte[]> poll = queue.poll(1, TimeUnit.SECONDS);
                if (poll != null) {
                    try {
                        onMessage(poll);
                    } catch (Exception ex) {
                        logger.error("onMessageInternal error", ex);
                    }
                    if (autoReleaseBlocking) {
                        blockingNum.decrementAndGet();
                    }
                }
            }
        } catch (InterruptedException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }


    public abstract void onMessage(ConsumerRecord<String, byte[]> consumerRecord);

    public int getBlockingNum() {
        return blockingNum.get();
    }

    public int getWorkQueueNum() {
        int num = 0;
        for (ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue : queues) {
            num += queue.size();
        }
        return num;
    }

}

class ConsumerRebalanceLogger implements ConsumerRebalanceListener {
    static final Logger logger = LoggerFactory.getLogger(ConsumerRebalanceLogger.class);

    final Consumer<String, byte[]> consumer;

    public ConsumerRebalanceLogger(Consumer<String, byte[]> consumer) {
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

