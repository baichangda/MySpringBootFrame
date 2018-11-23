import com.bcd.Application;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.single.TaskConst;
import com.bcd.sys.task.single.TaskUtil;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SysTaskTest {
    @org.junit.Test
    public void test(){
        TaskBean taskBean1=TaskUtil.registerTask("test1",1,(task)->{
            Thread.sleep(10000L);
            System.out.println("test1");
        });

//        TaskBean taskBean2=TaskUtil.registerTask("test2",1,(task)->{
//            try {
//                Thread.sleep(5000L);
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("test2");
//        });

        TaskBean taskBean3=TaskUtil.registerTask("test3",1,(task)->{
            Thread.sleep(100000L);
            System.out.println("test3");
        });

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TaskUtil.stopTask(true,taskBean3.getId());

        TaskConst.SYS_TASK_POOL.shutdown();
        while(!TaskConst.SYS_TASK_POOL.isTerminated()){
            try {
                TaskConst.SYS_TASK_POOL.awaitTermination(1L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
