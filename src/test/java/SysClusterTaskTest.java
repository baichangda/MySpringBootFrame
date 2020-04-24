import com.bcd.*;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskContext;
import com.bcd.sys.task.cluster.ClusterTaskUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SysClusterTaskTest {

    @Autowired
    Func1 func1;
    @Autowired
    Func2 func2;
    @Autowired
    Func3 func3;
    @Autowired
    Func4 func4;
    @Autowired
    Func5 func5;



    @org.junit.Test
    public void test() throws InterruptedException{
        Serializable t1= ClusterTaskUtil.registerTask(TaskContext.newClusterTaskContext(
                new TaskBean("测试1"),
                func1));
        Serializable t2= ClusterTaskUtil.registerTask(TaskContext.newClusterTaskContext(
                new TaskBean("测试2"),
                func2));
        Serializable t3= ClusterTaskUtil.registerTask(TaskContext.newClusterTaskContext(
                new TaskBean("测试3"),
                func3));
        Serializable t4= ClusterTaskUtil.registerTask(TaskContext.newClusterTaskContext(
                new TaskBean("测试4"),
                func4));
        Serializable t5= ClusterTaskUtil.registerTask(TaskContext.newClusterTaskContext(
                new TaskBean("测试5"),
                func5));
//
        Thread.sleep(3000L);
        Boolean[] res= ClusterTaskUtil.stopTask(t1,t5);
        System.out.println(res[0]+"   "+res[1]);

        while(CommonConst.SYS_TASK_POOL.getActiveCount()!=0){
            Thread.sleep(1000L);
        }
        System.out.println("===========all finished");
    }
}
