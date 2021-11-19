package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.TaskFunction;
import com.bcd.sys.task.TaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TestTaskFunction1 extends TaskFunction<TaskBean, Long> {
    Logger logger = LoggerFactory.getLogger(TestTaskFunction1.class);

    volatile boolean stop = false;

    @Override
    public boolean execute(TaskRunnable<TaskBean, Long> runnable) {
        int count = 0;
        while (!stop && count < 10) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                logger.info("{}", getName());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        return !stop;
    }

    @Override
    public boolean supportStop() {
        return true;
    }

    @Override
    public void stop(TaskRunnable<TaskBean, Long> runnable) {
        stop = true;
    }
}
