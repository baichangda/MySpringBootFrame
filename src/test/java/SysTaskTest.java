import com.bcd.Application;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.task.TaskUtil;
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
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("test1");
        },(task)->System.out.println(task.getName()+"开始了"),(task)->System.out.println(task.getName()+"成功了"),null);

        TaskBean taskBean2=TaskUtil.registerTask("test2",1,(task)->{
            try {
                Thread.sleep(5000L);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("test2");
        },(task)->System.out.println(task.getName()+"开始了"),(task)->System.out.println(task.getName()+"成功了"),null);

        TaskBean taskBean3=TaskUtil.registerTask("test3",1,(task)->{
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("test3");
        },(task)->System.out.println(task.getName()+"开始了"),(task)->System.out.println(task.getName()+"成功了"),null);
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TaskUtil.stopTask(taskBean3.getId());
        CommonConst.SYS_TASK_POOL.shutdown();
        while(!CommonConst.SYS_TASK_POOL.isTerminated()){
            try {
                CommonConst.SYS_TASK_POOL.awaitTermination(1L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
