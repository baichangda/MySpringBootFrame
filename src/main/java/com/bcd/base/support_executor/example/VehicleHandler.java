package com.bcd.base.support_executor.example;

import com.bcd.base.support_executor.MyExecutor;
import com.bcd.base.support_executor.MyExecutorGroup;
import com.bcd.base.support_executor.MyHandler;

public class VehicleHandler extends MyHandler<String> {
    public VehicleHandler(String id, MyExecutor executor) {
        super(id, executor);
    }

    @Override
    public void onMessage_safe(String msg) {
        System.out.println(Thread.currentThread().getName()+","+msg);
    }

    public static void main(String[] args) throws InterruptedException {
        MyExecutorGroup<VehicleHandler> executorGroup = new MyExecutorGroup<>(8, 100) {
            @Override
            public MyHandler<?> newHandler(String id, MyExecutor executor) {
                return new VehicleHandler(id, executor);
            }
        };
        VehicleHandler myHandler1 = executorGroup.initHandler("TEST0000000000000");
        myHandler1.onMessage("0");
        myHandler1.onMessage("00");
        VehicleHandler myHandler2 = executorGroup.initHandler("TEST0000000000001");
        myHandler2.onMessage("1");
        VehicleHandler myHandler3 = executorGroup.initHandler("TEST0000000000002");
        myHandler3.onMessage("2");
        VehicleHandler myHandler4 = executorGroup.initHandler("TEST0000000000003");
        myHandler4.onMessage("3");
        Thread.sleep(2000);
        executorGroup.destroy();
    }
}
