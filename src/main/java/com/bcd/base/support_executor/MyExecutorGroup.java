package com.bcd.base.support_executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MyExecutorGroup<T extends MyHandler<?>> {
    public final int num;

    public final MyExecutor[] executors;

    public final Map<String, MyHandler<?>> context = new ConcurrentHashMap<>();

    public final String name;

    public MyExecutorGroup(int num, int perExecutorQueueSize, String name) {
        this.num = num;
        this.executors = new MyExecutor[num];
        this.name = name;
        for (int i = 0; i < num; i++) {
            this.executors[i] = new MyExecutor(perExecutorQueueSize, "MyExecutor_" + name + "(" + num + ")_" + i);
        }
    }

    public abstract MyHandler<?> newHandler(String id, MyExecutor executor);

    public T initHandler(String id) {
        int i = Math.floorMod(id.hashCode(), num);
        MyExecutor executor = executors[i];
        MyHandler<?> myHandler = context.computeIfAbsent(id, k -> this.newHandler(id, executor));
        myHandler.init();
        return (T) myHandler;
    }

    public void destroyHandler(String id) {
        int i = Math.floorMod(id.hashCode(), num);
        MyHandler<?> remove = context.remove(id);
        remove.destroy();
    }

    public void destroy() {
        for (int i = 0; i < num; i++) {
            for (Map.Entry<String, MyHandler<?>> entry : context.entrySet()) {
                destroyHandler(entry.getKey());
            }
        }
        for (MyExecutor executor : executors) {
            executor.destroy();
        }
    }
}
