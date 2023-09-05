package com.bcd.base.support_kafka.nospring;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExecutorUtil;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
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
 * 不同的kafka版本表现不同
 * 1、版本>=0.10.0
 * {@link Consumer#offsetsForTimes(Map)}、{@link ConsumerRecord#timestamp()}有效
 * 此时会根据startTime找到offset、从此offset开始消费、直到endTime、然后自动结束、销毁资源
 * 2、版本<0.10.0
 * {@link Consumer#offsetsForTimes(Map)}、{@link ConsumerRecord#timestamp()}无效
 * 此时会从头开始消费、且无法自动结束退出、可以调用{@link #destroy()}触发退出
 */
@SuppressWarnings("unchecked")
public abstract class AbstractConsumerForTimeRange {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 工作线程数量
     */
    private final int workThreadNum;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    private final int workThreadQueueSize;

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

    private final long startTimeTs;
    private final long endTimeTs;

    /**
     * @param consumerProp        消费者属性
     * @param workThreadNum       工作线程个数
     * @param workThreadPerQueue  是否每一个工作线程都有一个队列
     *                            true时候、可以通过{@link #index(ConsumerRecord)}实现记录关联work线程、这在某些场景可以避免线程竞争
     *                            false时候、共享一个队列
     * @param workThreadQueueSize 工作队列的长度
     *                            最大实际阻塞量计算方式如下
     *                            当{@link #workThreadPerQueue}
     *                            为true时候、实际最大阻塞量是 {@link #workThreadQueueSize} * {@link #workThreadNum}
     *                            为false时候、实际最大阻塞量是 {@link #workThreadQueueSize}
     * @param autoReleaseBlocking 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param maxConsumeSpeed     最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     * @param startTimeTs         获取数据开始时间戳
     * @param endTimeTs           获取数据结束时间戳
     * @param topics              消费的topic
     */
    public AbstractConsumerForTimeRange(ConsumerProp consumerProp,
                                        int workThreadNum,
                                        boolean workThreadPerQueue,
                                        int workThreadQueueSize,
                                        boolean autoReleaseBlocking,
                                        int maxConsumeSpeed,
                                        long startTimeTs,
                                        long endTimeTs,
                                        String... topics) {
        this.workThreadNum = workThreadNum;
        this.workThreadPerQueue = workThreadPerQueue;
        this.workThreadQueueSize = workThreadQueueSize;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.startTimeTs = startTimeTs;
        this.endTimeTs = endTimeTs;
        this.topics = topics;

        this.consumer = new KafkaConsumer<>(this.consumerProperties(consumerProp));

        //初始化消费线程
        this.consumeThread = new Thread(this::consume);

        //初始化工作线程
        this.workThreads = new Thread[workThreadNum];

        //根据是否公用一个队列、来指定构造
        if (workThreadPerQueue) {
            this.queues = null;
            this.queue = new ArrayBlockingQueue<>(this.workThreadQueueSize);
            for (int i = 0; i < workThreadNum; i++) {
                workThreads[i] = new Thread(() -> work(this.queue));
            }
        } else {
            this.queue = null;
            this.queues = new ArrayBlockingQueue[workThreadNum];
            for (int i = 0; i < workThreadNum; i++) {
                final ArrayBlockingQueue<ConsumerRecord<String, byte[]>> queue = new ArrayBlockingQueue<>(this.workThreadQueueSize);
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
                    initConsumer(consumer);

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
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
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
    private void initConsumer(Consumer<String, byte[]> consumer) {
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

    /**
     * 消费
     *
     * @return
     */
    public void consume() {
        if (workThreadPerQueue) {
            while (running_consume) {
                try {
                    //检查阻塞
                    if (blockingNum.get() >= workThreadQueueSize) {
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
                    if (maxConsumeSpeed > 0) {
                        //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
                        final int curConsumeCount = consumeCount.addAndGet(count);
                        if (curConsumeCount >= maxConsumeSpeed) {
                            do {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } while (consumeCount.get() >= maxConsumeSpeed);
                        }
                    }

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
                        queues[index(consumerRecord)].put(consumerRecord);
                    }

                    //重新设置订阅
                    if (!removeSet.isEmpty()) {
                        final String reduce = removeSet.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + "," + e2).get();
                        logger.info("consumer assignment change, remove [{}]", reduce);
                        final Set<TopicPartition> newAssignment = new HashSet<>(consumer.assignment());
                        for (TopicPartition remove : removeSet) {
                            newAssignment.remove(remove);
                        }
                        consumer.assign(newAssignment);
                        //如果订阅为空、退出消费
                        if (newAssignment.isEmpty()) {
                            logger.info("consumer assignment empty, exit");
                            break;
                        }
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
                    if (blockingNum.get() >= workThreadQueueSize) {
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
                    if (maxConsumeSpeed > 0) {
                        //控制每秒消费、如果消费过快、则阻塞一会、放慢速度
                        final int curConsumeCount = consumeCount.addAndGet(count);
                        if (curConsumeCount >= maxConsumeSpeed) {
                            do {
                                TimeUnit.MILLISECONDS.sleep(50);
                            } while (consumeCount.get() >= maxConsumeSpeed);
                        }
                    }

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
                        queue.put(consumerRecord);
                    }

                    //重新设置订阅
                    if (!removeSet.isEmpty()) {
                        final String reduce = removeSet.stream().map(e -> e.topic() + ":" + e.partition()).reduce((e1, e2) -> e1 + "," + e2).get();
                        logger.info("consumer assignment change, remove [{}]", reduce);
                        final Set<TopicPartition> newAssignment = new HashSet<>(consumer.assignment());
                        for (TopicPartition remove : removeSet) {
                            newAssignment.remove(remove);
                        }
                        consumer.assign(newAssignment);
                        //如果订阅为空、退出消费
                        if (newAssignment.isEmpty()) {
                            logger.info("consumer assignment empty, exit");
                            break;
                        }
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


    public static void main(String[] args) {
        double d1 = 1;
        double d2 = 0.9;
        double d3 = 0.1;
        double d4 = 0.1;
        double d5 = 0.1;
        System.out.println((d1 - d2) == d3); //false
        System.out.println(d4 == d5); //true
        System.out.println(1 - 0.5);  //!=0.5
        System.out.println(1 - 0.9); //!=0.1
        System.out.println(0.30000000000000001 == 0.3);
    }
}

