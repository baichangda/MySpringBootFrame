package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.TaskService;
import com.bcd.sys.task.TaskBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

//@SpringBootTest(classes = Application.class)
public class Test {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    TaskService taskService;

    @org.junit.jupiter.api.Test
    public void test1() {
        TestTaskFunction1 testTaskFunction1 = new TestTaskFunction1();
        TestTaskFunction2 testTaskFunction2 = new TestTaskFunction2();
        final TaskBuilder<TaskBean, Long> builder =
                TaskBuilder.newBuilder("test", taskService)
                        .withPoolSize(1)
                        .build();
        TaskBean taskBean1 = new TaskBean("a");
        final Long id1 = builder.registerTask(taskBean1, testTaskFunction1);
        TaskBean taskBean2 = new TaskBean("b");
        final Long id2 = builder.registerTask(taskBean2, testTaskFunction2);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        builder.stopTask(id1, id2);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        builder.destroy();
    }


    @org.junit.jupiter.api.Test
    public void test2() {
        TestTaskFunction1 testTaskFunction1 = new TestTaskFunction1();
        TestTaskFunction2 testTaskFunction2 = new TestTaskFunction2();
        final TaskBuilder<TaskBean, Long> builder =
                TaskBuilder.newBuilder("test", taskService)
                        .withCluster("test", redisConnectionFactory)
                        .withPoolSize(1)
                        .build();
        TaskBean taskBean1 = new TaskBean("c");
        final Long id1 = builder.registerTask(taskBean1, testTaskFunction1);
        TaskBean taskBean2 = new TaskBean("d");
        final Long id2 = builder.registerTask(taskBean2, testTaskFunction2);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        builder.stopTask(id1, id2);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        builder.destroy();
    }

}
