package com.bcd.base.support_executor;

public abstract class MyHandler<T> {

    public final String id;
    public final MyExecutor executor;

    public MyHandler(String id, MyExecutor executor) {
        this.id = id;
        this.executor = executor;
    }

    public final void onMessage(T msg) {
        executor.execute(() -> onMessage_safe(msg));
    }

    public final void init() {
        executor.execute(this::init_safe);
    }

    public final void destroy() {
        executor.execute(this::destroy_safe);
    }

    public abstract void onMessage_safe(T msg);

    public void init_safe() {

    }
    public void destroy_safe() {

    }

}
