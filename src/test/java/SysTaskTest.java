import com.bcd.Application;
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.OrgService;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.cluster.TaskUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SysTaskTest {

    @Autowired
    OrgService orgService;

    @org.junit.Test
    public void test() throws InterruptedException{
        TaskBean t1=TaskUtil.registerTask("test1",1, taskBean -> {
            Thread.sleep(30*1000);
        });
        TaskBean t2=TaskUtil.registerTask("test2",1, taskBean -> {
            Thread.sleep(20*1000);
        });
        TaskBean t3=TaskUtil.registerTask("test3",1, taskBean -> {
            OrgBean orgBean=new OrgBean();
            orgBean.setName("absdfs");
//            orgService.save(orgBean);
        });
        Thread.sleep(2000L);
        Boolean[] res= TaskUtil.stopTask(true,t1.getId());
        System.out.println(res[0]);

        CommonConst.SYS_TASK_POOL.shutdown();
        while(!CommonConst.SYS_TASK_POOL.isTerminated()){
            CommonConst.SYS_TASK_POOL.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
