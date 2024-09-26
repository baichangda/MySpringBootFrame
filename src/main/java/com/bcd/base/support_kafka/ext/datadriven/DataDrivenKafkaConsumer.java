package com.bcd.base.support_kafka.ext.datadriven;

import com.bcd.base.exception.BaseException;
import com.bcd.base.support_kafka.ext.ConsumerProp;
import com.bcd.base.support_kafka.ext.ConsumerRebalanceLogger;
import com.bcd.base.util.DateUtil;
import com.bcd.base.util.ExecutorUtil;
import com.bcd.base.util.StringUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 此类要求提供 kafka-client即可、不依赖spring-kafka
 * 数据驱动模型
 * 即消费到数据后、每一个数据将会根据{@link #id(ConsumerRecord)}、{@link #getWorkExecutor(String)}分配到固定的{@link WorkExecutor}
 * 同时会通过{@link #newHandler(String, WorkExecutor)}构造数据对象
 * 后续{@link WorkHandler}中所有的操作都会由分配的{@link WorkExecutor}来执行
 * 这样做的好处是能保证{@link WorkHandler}所有方法都是线程安全的
 *
 * 此类会产生如下线程
 * <p>
 * 消费者线程可能有多个、开头为 {name}-consumer
 * 例如test-consumer(1/3)-partition(0)
 * consumer(1/3)代表有3个消费线程、这是第一个
 * partition(0)代表这个消费线程消费哪个分区
 * <p>
 * 工作任务执行器线程可能有多个、开头为 {name}-worker
 * 例如test-worker(1/3)
 * worker(1/3)代表有3个工作线程、这是第一个
 * <p>
 * 销毁资源钩子线程只有一个、开头为 {name}-shutdown
 * 例如test-shutdown
 * <p>
 * 监控信息线程只有一个、开头为 {name}-monitor
 * 需要开启{@link #monitor_period}才会有
 * 例如test-monitor
 * <p>
 * 工作任务执行器中的阻塞检查线程可能有多个、开头为 {name}-worker、以 -blockingChecker 结尾
 * 需要开启{@link #blockingChecker}才会有
 * 例如test-worker(1/3)-blockingChecker
 * 其中test-worker(1/3)即工作线程名称、接后缀 -blockingChecker
 * <p>
 * 限速重置消费计数线程只有一个、开头为 {name}-reset
 * 需要开启{@link #maxConsumeSpeed}才会有
 * 例如test-reset
 * <p>
 * 定时扫描过期workHandler线程只有一个、开头为 {name}-scanner
 * 需要开启{@link #workHandlerScanner}才会有
 * 例如test-scanner
 * <p>
 */
public abstract class DataDrivenKafkaConsumer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public final String name;
    public final Properties properties;

    /**
     * 消费线程
     */
    public Thread consumeThread;
    public Thread[] consumeThreads;

    /**
     * 工作执行器数量
     */
    public final int workExecutorNum;
    /**
     * 工作执行器非阻塞任务线程池队列大小
     */
    public final int workExecutorQueueSize;
    /**
     * 工作执行器阻塞检查任务执行周期
     */
    public final WorkExecutor.BlockingChecker blockingChecker;
    /**
     * 工作执行器数组
     */
    public final WorkExecutor[] workExecutors;

    /**
     * 最大阻塞(0代表不阻塞)
     */
    public final int maxBlockingNum;

    /**
     * 当前阻塞数量
     */
    public final LongAdder blockingNum = new LongAdder();

    /**
     * 是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     */
    public final boolean autoReleaseBlocking;

    /**
     * 最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     */
    public final int maxConsumeSpeed;
    public final AtomicInteger consumeCount;
    public ScheduledExecutorService resetConsumeCountPool;

    /**
     * 扫描过期{@link WorkHandler}参数
     */
    public final WorkHandlerScanner workHandlerScanner;
    public ScheduledExecutorService scannerPool;

    /**
     * 消费topic
     */
    public final String topic;

    /**
     * 消费topic的partition
     */
    public final int[] partitions;

    /**
     * 消费模式、由{@link #partitions}生成
     * 1、启动一个线程消费topic所有的分区
     * 2、获取topic所有的分区、每个分区启动一个线程消费指定分区
     * 3、依据{@link #partitions}、启动对应个数的线程消费指定的分区
     */
    public final int consumeMode;

    /**
     * 当前消费者是否可用
     */
    public volatile boolean available;

    /**
     * 控制退出线程标志
     */
    public volatile boolean running_consume = true;

    /**
     * 监控信息
     */
    public final int monitor_period;
    public final LongAdder monitor_workHandlerCount;
    public final LongAdder monitor_consumeCount;
    //处理数据数量(无论是否发生异常)
    public final LongAdder monitor_workCount;
    public ScheduledExecutorService monitor_pool;

    private Thread shutdownHookThread;

    public static class WorkHandlerScanner {
        public final int periodInSecond;
        public final int expiredInSecond;

        private WorkHandlerScanner(int periodInSecond, int expiredInSecond) {
            this.periodInSecond = periodInSecond;
            this.expiredInSecond = expiredInSecond;
        }

        /**
         * @param periodInSecond  定时任务扫描周期(秒)
         * @param expiredInSecond 判断workHandler过期的时间(秒)
         */
        public static WorkHandlerScanner get(int periodInSecond, int expiredInSecond) {
            return new WorkHandlerScanner(periodInSecond, expiredInSecond);
        }

    }


    /**
     * @param name                  当前消费者的名称(用于标定线程名称)
     * @param consumerProp          消费者属性
     * @param workExecutorNum       工作任务执行器个数
     * @param workExecutorQueueSize 工作任务执行器无阻塞任务线程池队列大小
     *                              <=0代表不限制、此时使用{@link LinkedBlockingQueue}
     *                              其他情况、则使用{@link ArrayBlockingQueue}
     *                              每个工作任务执行器都有一个自己的队列
     * @param blockingChecker       工作任务执行器阻塞检查参数
     *                              null代表不启动阻塞检查
     *                              否则会启动阻塞检查、每一个执行器会启动一个周期任务线程池、周期进行检查操作
     *                              检查逻辑为
     *                              向执行器中写入一个空任务、然后等待{@link com.bcd.base.support_kafka.ext.datadriven.WorkExecutor.BlockingChecker#expiredInSecond}后
     *                              检查任务是否完成、如果未完成、则告警并每秒执行一次检查、直到完成
     * @param maxBlockingNum        最大阻塞数量(0代表不限制)、当内存中达到最大阻塞数量时候、消费者会停止消费
     *                              当不限制时候、还是会记录{@link #blockingNum}、便于监控阻塞数量
     * @param autoReleaseBlocking   是否自动释放阻塞、适用于工作内容为同步处理的逻辑
     * @param maxConsumeSpeed       最大消费速度每秒(0代表不限制)、kafka一次消费一批数据、设置过小会导致不起作用、此时会每秒处理一批数据
     *                              每消费一次的数据量大小取决于如下消费者参数
     *                              {@link ConsumerConfig#MAX_POLL_RECORDS_CONFIG} 一次poll消费最大数据量
     *                              {@link ConsumerConfig#MAX_PARTITION_FETCH_BYTES_CONFIG} 每个分区最大拉取字节数
     * @param workHandlerScanner    定时扫描并销毁过期的{@link WorkHandler}、销毁时候会执行其{@link WorkHandler#destroy()}方法、由对应的工作任务执行器执行
     *                              null则代表不启动扫描
     * @param monitor_period        监控信息打印周期(秒)、0则代表不打印
     * @param topic                 消费的topic
     * @param partitions            消费的topic的分区、不同的情况消费策略不一样
     *                              如果partitions为空、则会启动单线程即一个消费者使用{@link KafkaConsumer#subscribe(Pattern)}完成订阅
     *                              如果partitions不为空、且partitions[0]<0、则会首先通过{@link KafkaConsumer#partitionsFor(String)}获取分区个数、然后启动对应的消费线程、每一个线程一个消费者使用{@link KafkaConsumer#assign(Collection)}完成分配
     *                              其他情况、则根据指定分区个数启动对应个数的线程、每个线程负责消费一个分区
     */
    public DataDrivenKafkaConsumer(String name,
                                   ConsumerProp consumerProp,
                                   int workExecutorNum,
                                   int workExecutorQueueSize,
                                   WorkExecutor.BlockingChecker blockingChecker,
                                   int maxBlockingNum,
                                   boolean autoReleaseBlocking,
                                   int maxConsumeSpeed,
                                   WorkHandlerScanner workHandlerScanner,
                                   int monitor_period,
                                   String topic,
                                   int... partitions) {
        this.name = name;
        this.properties = consumerProp.toProperties();
        this.workExecutorNum = workExecutorNum;
        this.workExecutorQueueSize = workExecutorQueueSize;
        this.blockingChecker = blockingChecker;
        this.maxBlockingNum = maxBlockingNum;
        this.autoReleaseBlocking = autoReleaseBlocking;
        this.maxConsumeSpeed = maxConsumeSpeed;
        this.workHandlerScanner = workHandlerScanner;
        this.monitor_period = monitor_period;
        this.topic = topic;
        this.partitions = partitions;
        if (partitions.length == 0) {
            consumeMode = 1;
        } else {
            if (partitions[0] < 0) {
                consumeMode = 2;
            } else {
                consumeMode = 3;
            }
        }

        if (monitor_period == 0) {
            monitor_workHandlerCount = null;
            monitor_consumeCount = null;
            monitor_workCount = null;
        } else {
            monitor_workHandlerCount = new LongAdder();
            monitor_consumeCount = new LongAdder();
            monitor_workCount = new LongAdder();
        }

        if (maxConsumeSpeed > 0) {
            consumeCount = new AtomicInteger();
        } else {
            consumeCount = null;
        }

        //初始化工作任务执行器
        this.workExecutors = new WorkExecutor[workExecutorNum];
        for (int i = 0; i < workExecutorNum; i++) {
            this.workExecutors[i] = new WorkExecutor(name + "-worker(" + (i + 1) + "/" + workExecutorNum + ")", workExecutorQueueSize, blockingChecker);
        }

    }

    /**
     * 根据消费的数据获取其id
     * 可以由子类重写
     *
     * @param consumerRecord
     * @return
     */
    protected String id(ConsumerRecord<String, byte[]> consumerRecord) {
        return consumerRecord.key();
    }

    /**
     * 根据id分配到对应的工作执行者上
     *
     * @param id
     * @return
     */
    public WorkExecutor getWorkExecutor(String id) {
        int index = Math.floorMod(id.hashCode(), workExecutorNum);
        return workExecutors[index];
    }

    /**
     * 根据id和分配的执行器构造WorkHandler
     *
     * @param id
     * @param executor
     * @return
     */
    public abstract WorkHandler newHandler(String id, WorkExecutor executor);

    /**
     * 移除workHandler
     *
     * @param id
     * @return
     */
    public final CompletableFuture<Void> removeHandler(String id) {
        WorkExecutor workExecutor = getWorkExecutor(id);
        return workExecutor.execute(() -> {
            WorkHandler workHandler = workExecutor.workHandlers.remove(id);
            removeHandlerInExecutor(workHandler);
        });
    }

    private void removeHandlerInExecutor(WorkHandler workHandler) {
        if (workHandler != null) {
            try {
                workHandler.destroy();
            } catch (Exception ex) {
                logger.error("workHandler destroy error id[{}]", workHandler.id, ex);
            }
            if (monitor_period > 0) {
                monitor_workHandlerCount.decrement();
            }

        }
    }

    /**
     * 根据id获取对应WorkHandler
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <V extends WorkHandler> V getHandler(String id) {
        WorkExecutor workExecutor = getWorkExecutor(id);
        try {
            return (V) workExecutor.submit(() -> workExecutor.workHandlers.get(id)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw BaseException.get(e);
        }
    }

    /**
     * 根据分区个数
     * 启动多个消费者线程
     * 分区-消费者-线程 一一对应
     *
     * @param consumer
     * @param ps
     */
    private void startConsumePartitions(KafkaConsumer<String, byte[]> consumer, int[] ps) {
        if (ps.length == 0) {
            consumer.close();
        } else {
            int partitionSize = ps.length;
            KafkaConsumer<String, byte[]>[] consumers = new KafkaConsumer[partitionSize];
            try {
                int firstPartition = ps[0];
                consumer.assign(Collections.singletonList(new TopicPartition(topic, firstPartition)));
                consumers[0] = consumer;
                for (int i = 1; i < partitionSize; i++) {
                    int partition = ps[i];
                    final KafkaConsumer<String, byte[]> cur = new KafkaConsumer<>(properties);
                    cur.assign(Collections.singletonList(new TopicPartition(topic, partition)));
                    consumers[i] = cur;
                }
            } catch (Exception ex) {
                //发生异常则关闭之前构造的消费者
                for (KafkaConsumer<String, byte[]> cur : consumers) {
                    if (cur != null) {
                        cur.close();
                    }
                }
                throw BaseException.get(ex);
            }
            consumeThreads = new Thread[partitionSize];
            for (int i = 0; i < partitionSize; i++) {
                final KafkaConsumer<String, byte[]> cur = consumers[i];
                Thread thread = new Thread(() -> consume(cur), name + "-consumer(" + (i + 1) + "/" + partitionSize + ")-partition(" + i + ")");
                consumeThreads[i] = thread;
                thread.start();
            }
        }
        logger.info("start consumers[{}] for partitions[{}]", partitions.length, Arrays.stream(partitions).mapToObj(e -> topic + ":" + e).collect(Collectors.joining(",")));
    }

    /**
     * 初始化
     * 构造线程池
     * 启动线程
     * 注册销毁狗子
     */
    public void init() {
        if (!available) {
            synchronized (this) {
                if (!available) {
                    try {
                        //标记可用
                        available = true;
                        //初始化重置消费计数线程池(如果有限制最大消费速度)、提交工作任务、每秒重置消费数量
                        if (maxConsumeSpeed > 0) {
                            resetConsumeCountPool = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, name + "-reset"));
                            resetConsumeCountPool.scheduleAtFixedRate(() -> {
                                consumeCount.set(0);
                            }, 1, 1, TimeUnit.SECONDS);
                        }
                        //启动监控
                        if (monitor_period != 0) {
                            monitor_pool = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, name + "-monitor"));
                            monitor_pool.scheduleAtFixedRate(() -> logger.info(monitor_log()), monitor_period, monitor_period, TimeUnit.SECONDS);
                        }
                        //启动扫描过期数据
                        if (workHandlerScanner != null) {
                            scannerPool = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, name + "-scanner"));
                            scannerPool.scheduleAtFixedRate(() -> scanAndDestroyWorkHandler(workHandlerScanner.expiredInSecond), workHandlerScanner.periodInSecond, workHandlerScanner.periodInSecond, TimeUnit.SECONDS);
                        }
                        //启动任务执行器
                        for (WorkExecutor workExecutor : this.workExecutors) {
                            workExecutor.init();
                        }
                        //启动消费者
                        switch (consumeMode) {
                            case 1: {
                                final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                                consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceLogger(consumer));
                                //初始化消费线程、提交消费任务
                                consumeThread = new Thread(() -> consume(consumer), name + "-consumer(1/1)-partition(all)");
                                consumeThread.start();
                                logger.info("start consumer for topic[{}]", topic);
                                break;
                            }
                            case 2: {
                                final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                                int[] ps = consumer.partitionsFor(topic).stream().mapToInt(PartitionInfo::partition).toArray();
                                startConsumePartitions(consumer, ps);
                                break;
                            }
                            default: {
                                final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                                startConsumePartitions(consumer, partitions);
                                break;
                            }
                        }
                        //增加销毁回调
                        shutdownHookThread = new Thread(this::destroy, name + "-shutdown");
                        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
                    } catch (Exception ex) {
                        //初始化异常、则销毁资源
                        destroy();
                        throw BaseException.get(ex);
                    }
                }
            }
        }
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        if (available) {
            synchronized (this) {
                if (available) {
                    //打上退出标记、等待消费线程退出
                    running_consume = false;
                    ExecutorUtil.shutdownThenAwait(consumeThread, consumeThreads, resetConsumeCountPool);
                    consumeThread = null;
                    consumeThreads = null;
                    resetConsumeCountPool = null;
                    //等待工作执行器退出
                    for (WorkExecutor workExecutor : workExecutors) {
                        //先销毁所有handler
                        workExecutor.execute(() -> {
                            Set<String> keySet = workExecutor.workHandlers.keySet();
                            for (String key : keySet) {
                                removeHandler(key);
                            }
                        });
                        workExecutor.destroy();
                    }
                    //取消监控、扫描过期线程
                    ExecutorUtil.shutdownAllThenAwait(monitor_pool, scannerPool);
                    monitor_pool = null;
                    scannerPool = null;

                    //取消shutdownHook
                    if (shutdownHookThread != null) {
                        try {
                            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                        } catch (IllegalStateException ex) {
                            throw BaseException.get(ex);
                        }
                    }
                    shutdownHookThread = null;
                    //标记不可用
                    available = false;

                }
            }
        }
    }


    /**
     * 消费
     */
    private void consume(KafkaConsumer<String, byte[]> consumer) {
        try {
            while (running_consume) {
                try {
                    //检查阻塞
                    if (maxBlockingNum > 0) {
                        if (blockingNum.sum() >= maxBlockingNum) {
                            TimeUnit.MILLISECONDS.sleep(100);
                            continue;
                        }
                    }
                    //消费一批数据
                    final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(3));
                    if (consumerRecords == null || consumerRecords.isEmpty()) {
                        continue;
                    }

                    //统计
                    final int count = consumerRecords.count();
                    blockingNum.add(count);
                    if (monitor_period > 0) {
                        monitor_consumeCount.add(count);
                    }

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
                        final String id = id(consumerRecord);
                        WorkExecutor workExecutor = getWorkExecutor(id);
                        //交给执行器处理
                        workExecutor.execute(() -> {
                            //首先获取workHandler
                            WorkHandler workHandler = workExecutor.workHandlers.computeIfAbsent(id, k -> {
                                //初始化workHandler
                                try {
                                    WorkHandler temp = newHandler(id, workExecutor);
                                    temp.init();
                                    if (monitor_period > 0) {
                                        monitor_workHandlerCount.increment();
                                    }
                                    return temp;
                                } catch (Exception ex) {
                                    //初始化workHandler失败时候、释放阻塞
                                    blockingNum.decrement();
                                    logger.error("workHandler init error id[{}]", id, ex);
                                    return null;
                                }
                            });
                            //处理数据
                            if (workHandler != null) {
                                workHandler.lastMessageTime = DateUtil.CacheSecond.current();
                                try {
                                    workHandler.onMessage(consumerRecord);
                                } catch (Exception ex) {
                                    logger.error("workHandler onMessage error id[{}]", id, ex);
                                }
                                if (autoReleaseBlocking) {
                                    blockingNum.decrement();
                                }
                                if (monitor_period > 0) {
                                    monitor_workCount.increment();
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.error("kafka consumer topic[{}] cycle error,try again after 3s", topic, ex);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        throw BaseException.get(e);
                    }
                }
            }
        } finally {
            String assignment = consumer.assignment().stream().map(e -> e.topic() + ":" + e.partition()).collect(Collectors.joining(","));
            logger.info("consumer[{}] assignment[{}] close", this.getClass().getName(), assignment);
            consumer.close();
        }

    }

    private String getQueueLog(WorkExecutor executor) {
        return executor.blockingQueue.size() + (workExecutorQueueSize > 0 ? ("/" + workExecutorQueueSize) : "");
    }

    /**
     * 监控日志
     * 如果需要修改日志、可以重写此方法
     * <p>
     * workExecutor 任务执行器个数
     * workHandler 任务处理器个数
     * blocking 当前阻塞数量/最大阻塞数量
     * consume 所有的消费者合计消费速度(每秒)
     * queues 所有执行器的队列情况、每个执行器 当前队列大小/最大队列大小(如果有最大队列大小)
     * work 所有的任务执行器合计处理速度(每秒)
     */
    public String monitor_log() {
        return StringUtil.format("name[{}] " +
                        "workExecutor[{}] " +
                        "workHandler[{}] " +
                        "blocking[{}/{}] " +
                        "consume[{}/s] " +
                        "queues[{}] " +
                        "work[{}/s]",
                name,
                workExecutors.length,
                monitor_workHandlerCount.sum(),
                blockingNum.sum(), maxBlockingNum,
                monitor_consumeCount.sumThenReset() / monitor_period,
                Arrays.stream(workExecutors).map(this::getQueueLog).collect(Collectors.joining(" ")),
                monitor_workCount.sumThenReset() / monitor_period);
    }

    /**
     * 扫描并销毁过期的workHandler
     *
     * @param expiredInSecond 过期时间
     */
    public final void scanAndDestroyWorkHandler(int expiredInSecond) {
        long ts = DateUtil.CacheSecond.current() - expiredInSecond;
        for (WorkExecutor workExecutor : workExecutors) {
            workExecutor.execute(() -> {
                for (WorkHandler workHandler : workExecutor.workHandlers.values()) {
                    if (workHandler.lastMessageTime < ts) {
                        removeHandlerInExecutor(workHandler);
                    }
                }
            });
        }
    }
}

