package com.bcd.base.support_executor;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.ExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MyExecutor {

    private final AtomicInteger poolNumber = new AtomicInteger(1);

    public final ThreadPoolExecutor executor;

    public MyExecutor(int queueSize) {
        this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    public void destroy() {
        ExecutorUtil.shutdown(executor);
    }
}
