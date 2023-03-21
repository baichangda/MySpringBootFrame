package com.bcd.base.support_disruptor;

import com.bcd.base.util.JsonUtil;
import com.bcd.sys.bean.UserBean;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class MyDisruptor<T> {

    Disruptor<Event<T>> disruptor;
    EventTranslatorOneArg<Event<T>, T> eventTranslator = (event, sequence, t) -> event.t = t;
    EventFactory<Event<T>> eventFactory = Event::new;
    ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;

    /**
     *
     * @param maxNum 必需是2的倍数、如果不是则向上取2的倍数
     * @param producerType
     * @param waitStrategy
     */
    public MyDisruptor(int maxNum, ProducerType producerType, WaitStrategy waitStrategy) {
        this.disruptor = new Disruptor<>(eventFactory, pow2(maxNum), threadFactory, producerType, waitStrategy);

    }

    private int pow2(int n) {
        return (-1 >>> Integer.numberOfLeadingZeros(n - 1)) + 1;
    }

    public MyDisruptor<T> handle(Consumer<T>... consumers) {
        WorkHandler<Event<T>>[] workHandlers=new WorkHandler[consumers.length];
        for (int i = 0; i < consumers.length; i++) {
            final Consumer<T> consumer = consumers[i];
            workHandlers[i]= event -> consumer.accept(event.t);
        }
        disruptor.handleEventsWithWorkerPool(workHandlers);
        return this;
    }

    public MyDisruptor<T> init() {
        disruptor.start();
        return this;
    }

    public void destroy() {
        disruptor.shutdown();
    }

    public void publish(T t) {
        disruptor.publishEvent(eventTranslator, t);
    }

    public static void main(String[] args) throws InterruptedException {
        MyDisruptor<UserBean> myDisruptor = new MyDisruptor<>(128, ProducerType.MULTI, new BlockingWaitStrategy());
        myDisruptor.handle(e -> {
            System.out.println(JsonUtil.toJson(e));
        }).init();
        final UserBean userBean = new UserBean();
        userBean.realName = "test1";
        myDisruptor.publish(userBean);
        myDisruptor.destroy();
    }

}

class Event<T> {
    T t;
}



