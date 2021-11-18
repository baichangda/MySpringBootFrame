package com.bcd.base.support_disruptor;

import com.bcd.base.util.JsonUtil;
import com.bcd.sys.bean.UserBean;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.catalina.User;

import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class MyDisruptor<T> {

    Consumer<T> consumer;
    int maxNum;
    WaitStrategy waitStrategy = new BlockingWaitStrategy();
    ProducerType producerType = ProducerType.MULTI;

    Disruptor<Event<T>> disruptor;
    EventTranslatorOneArg<Event<T>, T> eventTranslator = (event, sequence, t) -> event.t = t;
    EventFactory<Event<T>> eventFactory = Event::new;
    ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;

    private MyDisruptor(Consumer<T> consumer, int maxNum) {
        this.consumer = consumer;
        this.maxNum = maxNum;

    }

    public static <T> MyDisruptor<T> newInstance(Consumer<T> consumer, int maxNum) {
        return new MyDisruptor<>(consumer, maxNum);
    }

    public MyDisruptor<T> setWaitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
        return this;
    }

    public MyDisruptor<T> setProducerType(ProducerType producerType) {
        this.producerType = producerType;
        return this;
    }

    private int pow2(int n) {
        return (-1 >>> Integer.numberOfLeadingZeros(n - 1)) + 1;
    }

    public MyDisruptor<T> init() {
        EventHandler<Event<T>> eventHandler = (event, sequence, endOfBatch) -> consumer.accept(event.t);
        disruptor = new Disruptor<>(eventFactory, pow2(maxNum), threadFactory, producerType, waitStrategy);
        disruptor.handleEventsWith(eventHandler);
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
        MyDisruptor<UserBean> myDisruptor = MyDisruptor.<UserBean>newInstance(t -> {
                    System.out.println(JsonUtil.toJson(t));
                }, 128)
                .setProducerType(ProducerType.SINGLE)
                .setWaitStrategy(new SleepingWaitStrategy())
                .init();
        final UserBean userBean = new UserBean();
        userBean.setRealName("test1");
        myDisruptor.publish(userBean);
        myDisruptor.destroy();
    }

}

class Event<T> {
    T t;
}



