import com.bcd.Application;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.OrgService;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskUtil;
import com.bcd.sys.task.single.SingleTaskUtil;
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
        Serializable t1= SingleTaskUtil.registerTask(new TaskBean("测试1"), taskBean -> {
            Thread.sleep(20*1000L);
            System.out.println("测试1");
            return taskBean;
        });
        Serializable t2=SingleTaskUtil.registerTask(new TaskBean("测试2"), taskBean -> {
            Thread.sleep(20*1000L);
            System.out.println("测试2");
            return taskBean;

        });
        Serializable t3=SingleTaskUtil.registerTask(new TaskBean("测试3"), taskBean -> {
            OrgBean orgBean=new OrgBean();
            orgBean.setName("asdfasd");
            orgBean.setCode("asdfasd");
            orgService.save(orgBean);
            Thread.sleep(20*1000L);
            return taskBean;
        });

        Serializable t4=SingleTaskUtil.registerTask(new TaskBean("测试4"), taskBean -> {
            Thread.sleep(20*1000L);
            System.out.println("测试4");
            return taskBean;

        });

        Serializable t5=SingleTaskUtil.registerTask(new TaskBean("测试5"), taskBean -> {
            throw BaseRuntimeException.getException("t5执行出错");
        });

        Thread.sleep(2000L);
        boolean[] res= SingleTaskUtil.stopTask(true,t1,t3);
        System.out.println(res[0]+" "+res[1]);

       while(CommonConst.SYS_TASK_POOL.getActiveCount()!=0){
            System.out.println(CommonConst.SYS_TASK_POOL.getActiveCount());
            Thread.sleep(1000L);
        }
        System.out.println("===========all finished");
    }
}
