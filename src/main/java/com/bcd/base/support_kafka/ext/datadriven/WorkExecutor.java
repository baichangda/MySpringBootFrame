package com.bcd.base.support_kafka.ext.datadriven;

import com.bcd.base.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class WorkExecutor {

    static Logger logger = LoggerFactory.getLogger(WorkExecutor.class);

    public final ThreadPoolExecutor executor;

    /**
     * 存储本执行器所有的handler
     */
    final Map<String, WorkHandler> workHandlers = new HashMap<>();

    public WorkExecutor(String threadName) {
        this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> new Thread(r, threadName));
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public CompletableFuture<Void> execute_cf(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    public <T> CompletableFuture<T> submit_cf(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public void destroy() {
        ExecutorUtil.shutdown(executor);
    }
}
