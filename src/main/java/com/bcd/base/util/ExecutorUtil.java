package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ExecutorUtil {

    /**
     * 循环从队列里面拿数据、堆积一批到缓存中、然后触发回调
     * 如下两种情况触发回调
     * 1、缓存数量到达期望数量、此时数量为except
     * 2、当队列数量为空、即将进入阻塞前一刻、此时数量<except
     *
     * @param queue    阻塞队列
     * @param except   期望数量
     * @param callback 回调方法
     * @param running  是否运行中、通过设置此为false打断循环
     * @param <T>
     */
    public static <T> void loop(BlockingQueue<T> queue, int except, Consumer<ArrayList<T>> callback, AtomicBoolean running) {
        try {
            final ArrayList<T> cache = new ArrayList<>(except);
            while (running.get()) {
                T t = queue.poll(3, TimeUnit.SECONDS);
                if (t == null) {
                    continue;
                }
                do {
                    cache.add(t);
                    if (cache.size() == except) {
                        callback.accept(cache);
                        cache.clear();
                    }
                    t = queue.poll();
                } while (t != null);
                callback.accept(cache);
                cache.clear();
            }
        } catch (InterruptedException ex) {
            throw BaseRuntimeException.getException(ex);
        }
    }

    public static void shutdown(Object... args) {
        if (args == null) {
            return;
        }
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof ExecutorService pool) {
                    shutdownThenAwait(pool);
                } else if (arg instanceof ExecutorService[] pools) {
                    shutdownThenAwait(pools);
                } else if (arg instanceof BlockingQueue<?> queue) {
                    awaitQueueEmpty(queue);
                } else if (arg instanceof BlockingQueue<?>[] queues) {
                    awaitQueueEmpty(queues);
                } else {
                    throw BaseRuntimeException.getException("arg type[{}] not support", arg.getClass().getName());
                }
            }
        }
    }


    /**
     * @param pools
     */
    public static void shutdownThenAwait(ExecutorService... pools) {
        if (pools == null) {
            return;
        }
        for (ExecutorService pool : pools) {
            pool.shutdown();
            try {
                while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {

                }
            } catch (InterruptedException ex) {
                throw BaseRuntimeException.getException(ex);
            }
        }
    }

    public static void awaitQueueEmpty(Queue<?>... queues) {
        if (queues == null) {
            return;
        }
        for (Queue<?> queue : queues) {
            while (!queue.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200L);
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
        }
    }

}
