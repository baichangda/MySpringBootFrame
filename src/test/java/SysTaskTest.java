import com.bcd.Application;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.OrgService;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SysTaskTest {

    @Autowired
    OrgService orgService;

    @org.junit.Test
    public void test() throws InterruptedException{
        Serializable t1=TaskUtil.registerTask(new TaskBean("测试1"), taskBean -> {
            try {
                Thread.sleep(20*1000L);
                System.out.println("测试1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return taskBean;
        });
        Serializable t2=TaskUtil.registerTask(new TaskBean("测试2"), taskBean -> {
            try {
                Thread.sleep(20*1000L);
                System.out.println("测试2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return taskBean;

        });
        Serializable t3=TaskUtil.registerTask(new TaskBean("测试3"), taskBean -> {
            OrgBean orgBean=new OrgBean();
            orgBean.setName("asdfasd");
            orgService.save(orgBean);
            try {
                Thread.sleep(20*1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return taskBean;
        });

        Serializable t4=TaskUtil.registerTask(new TaskBean("测试4"), taskBean -> {
            try {
                Thread.sleep(20*1000L);
                System.out.println("测试4");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return taskBean;

        });

        Serializable t5=TaskUtil.registerTask(new TaskBean("测试5"), taskBean -> {
            throw BaseRuntimeException.getException("t5执行出错");
        });

        Thread.sleep(2000L);
        Boolean[] res= TaskUtil.stopTask(true,t1);
        System.out.println(res[0]);

//        CommonConst.SYS_TASK_POOL.shutdown();
        while(!CommonConst.SYS_TASK_POOL.isTerminated()){
            CommonConst.SYS_TASK_POOL.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
