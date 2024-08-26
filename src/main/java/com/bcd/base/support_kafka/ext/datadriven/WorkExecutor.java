package com.bcd.base.support_kafka.ext.datadriven;

import com.bcd.base.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 工作执行器
 * 通过两个线程执行不同的任务
 * <p>
 * - {@link #executor}执行非阻塞任务
 * 调用如下方法
 * {@link #execute(Runnable)}
 * {@link #submit(Supplier)}
 * <p>
 * - {@link #blockingExecutor}执行阻塞任务
 * 调用如下方法
 * {@link #executeBlocking(Runnable)}
 * {@link #submitBlocking(Supplier)}
 * <p>
 * 注意:
 * 非阻塞任务线程中的任务不能阻塞、且任务之间是串行执行的、没有线程安全问题
 * 阻塞任务线程中的任务可以阻塞、任务之间是串行执行的、没有线程安全问题
 * 但是不同线程之间是并发执行的、有线程安全问题
 */
public class WorkExecutor {

    static Logger logger = LoggerFactory.getLogger(WorkExecutor.class);

    public final ThreadPoolExecutor executor;
    public final ThreadPoolExecutor blockingExecutor;

    /**
     * 存储本执行器所有的handler
     */
    final Map<String, WorkHandler> workHandlers = new HashMap<>();

    /**
     * 构造任务执行器
     *
     * @param threadName
     * @param queueSize  无阻塞任务线程池队列大小
     *                   0则使用{@link LinkedBlockingQueue}
     *                   否则使用{@link ArrayBlockingQueue}
     */
    public WorkExecutor(String threadName, int queueSize) {
        BlockingQueue<Runnable> blockingQueue;
        if (queueSize <= 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else {
            blockingQueue = new ArrayBlockingQueue<>(queueSize);
        }
        this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, blockingQueue, r -> new Thread(r, threadName),
                (r, executor) -> {
                    if (!executor.isShutdown()) {
                        try {
//                    logger.warn("workThread[{}] RejectedExecutionHandler",threadName);
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
        String blockingThreadName = threadName + "-blocking";
        this.blockingExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> new Thread(r, blockingThreadName));
    }

    public final CompletableFuture<Void> execute(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public final <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public final CompletableFuture<Void> executeBlocking(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, blockingExecutor);
    }

    public final <T> CompletableFuture<T> submitBlocking(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, blockingExecutor);
    }

    public void destroy() {
        ExecutorUtil.shutdownAllThenAwait(executor, blockingExecutor);
    }
}
