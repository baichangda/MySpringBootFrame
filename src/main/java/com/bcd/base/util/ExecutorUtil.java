package com.bcd.base.util;


import com.bcd.base.exception.BaseException;

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
            throw BaseException.get(ex);
        }
    }

    /**
     * 关闭资源、包括如下情况
     * 1、关闭线程池、等待线程执行完毕
     * 2、关闭队列、等待队列为空
     * 3、关闭线程、等待线程执行完毕
     * @param args
     */
    public static void shutdown(Object... args) {
        if (args == null || args.length == 0) {
            return;
        }
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof ExecutorService pool) {
                    shutdownThenAwait(pool);
                } else if (arg instanceof ExecutorService[] pools) {
                    shutdownThenAwait(pools);
                } else if (arg instanceof Thread thread) {
                    awaitThread(thread);
                } else if (arg instanceof Thread[] threads) {
                    awaitThread(threads);
                } else if (arg instanceof BlockingQueue<?> queue) {
                    awaitQueueEmpty(queue);
                } else if (arg instanceof BlockingQueue<?>[] queues) {
                    awaitQueueEmpty(queues);
                } else {
                    throw BaseException.get("arg type[{}] not support", arg.getClass().getName());
                }
            }
        }
    }


    /**
     * 关闭线程池并等待线程执行完毕
     * @param pools
     */
    public static void shutdownThenAwait(ExecutorService... pools) {
        if (pools == null || pools.length == 0) {
            return;
        }
        try {
            for (ExecutorService pool : pools) {
                if (pool != null && !pool.isTerminated()) {
                    pool.shutdown();
                    while (!pool.awaitTermination(60, TimeUnit.SECONDS)) {

                    }
                }
            }
        } catch (InterruptedException ex) {
            throw BaseException.get(ex);
        }
    }

    /**
     * 等待队列为空
     * @param queues
     */
    public static void awaitQueueEmpty(Queue<?>... queues) {
        if (queues == null || queues.length == 0) {
            return;
        }
        try {
            for (Queue<?> queue : queues) {
                while (queue != null && !queue.isEmpty()) {
                    TimeUnit.MILLISECONDS.sleep(100L);
                }
            }
        } catch (InterruptedException e) {
            throw BaseException.get(e);
        }
    }

    /**
     * 等待线程执行完毕
     * @param threads
     */
    public static void awaitThread(Thread... threads) {
        if (threads == null || threads.length == 0) {
            return;
        }
        try {
            for (Thread thread : threads) {
                if (thread != null) {
                    thread.join();
                }
            }
        } catch (InterruptedException e) {
            throw BaseException.get(e);
        }
    }

}
