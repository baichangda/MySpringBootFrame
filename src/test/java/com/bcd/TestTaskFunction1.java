package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.base.support_task.TaskFunction;
import com.bcd.base.support_task.TaskRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TestTaskFunction1 extends TaskFunction<TaskBean, Long> {
    Logger logger = LoggerFactory.getLogger(TestTaskFunction1.class);

    @Override
    public void execute(TaskRunnable<TaskBean, Long> runnable) {
        int count = 0;
        while (!runnable.isStop() && count < 10) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                logger.info("{}", getName());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    @Override
    public boolean supportStop() {
        return true;
    }


}
