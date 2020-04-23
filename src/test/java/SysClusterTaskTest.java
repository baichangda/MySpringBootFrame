import com.bcd.*;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.cluster.ClusterTaskUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SysClusterTaskTest {



    @org.junit.Test
    public void test() throws InterruptedException{
        Serializable t1= ClusterTaskUtil.registerTask(new TaskBean("测试1"), Func1.NAME, "集群测试1参数");
        Serializable t2=ClusterTaskUtil.registerTask(new TaskBean("测试2"), Func2.NAME,"集群测试2参数");
        Serializable t3=ClusterTaskUtil.registerTask(new TaskBean("测试3"), Func3.NAME,"集群测试3参数");
        Serializable t4=ClusterTaskUtil.registerTask(new TaskBean("测试4"), Func4.NAME,"集群测试4参数");
        Serializable t5=ClusterTaskUtil.registerTask(new TaskBean("测试5"), Func5.NAME,"集群测试5参数");
//
        Thread.sleep(3000L);
        Boolean[] res= ClusterTaskUtil.stopTask(t2,t5);
        System.out.println(res[0]+"   "+res[1]);

        while(CommonConst.SYS_TASK_POOL.getActiveCount()!=0){
            System.out.println(CommonConst.SYS_TASK_POOL.getActiveCount());
            Thread.sleep(1000L);
        }
        System.out.println("===========all finished");
    }
}
