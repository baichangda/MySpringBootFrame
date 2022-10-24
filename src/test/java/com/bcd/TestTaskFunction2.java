package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.base.support_task.TaskFunction;
import com.bcd.base.support_task.TaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TestTaskFunction2 extends TaskFunction<TaskBean,Long> {
    Logger logger = LoggerFactory.getLogger(TestTaskFunction2.class);

    @Override
    public void execute(TaskRunnable<TaskBean,Long> runnable) {
        try {
            TimeUnit.SECONDS.sleep(10);
            logger.info("{}", getName());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
