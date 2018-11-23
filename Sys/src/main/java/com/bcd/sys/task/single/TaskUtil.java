package com.bcd.sys.task.single;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import com.bcd.sys.task.TaskConsumer;
import com.bcd.sys.task.TaskStatus;
import com.bcd.sys.util.ShiroUtil;

import java.util.Date;
import java.util.concurrent.Future;

public class TaskUtil {
    /**
     * 注册任务
     * @param name 任务名称
     * @param type 任务类型 (对应TaskBean里面的type,根据不同的项目翻译成不同的意思,默认 1:普通任务;2:文件类型任务 )
     * @param consumer 任务执行方法
     * @return
     */
    public static TaskBean registerTask(String name, int type, TaskConsumer consumer){
        UserBean userBean= ShiroUtil.getCurrentUser();
        TaskBean taskBean=new TaskBean(name,type);
        if(userBean!=null){
            taskBean.setCreateUserName(userBean.getUsername());
            taskBean.setCreateUserId(userBean.getId());
        }
        taskBean.setCreateTime(new Date());
        taskBean.setStatus(TaskStatus.WAITING.getStatus());
        taskBean.setConsumer(consumer);
        CommonConst.Init.taskService.save(taskBean);
        Future future= TaskConst.SYS_TASK_POOL.submit(new SysTaskRunnable(taskBean));
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(taskBean.getId(),future);
        return taskBean;
    }

    /**
     * 停止任务
     * @param mayInterruptIfRunning 是否打断正在运行的任务(true表示打断wait或者sleep的任务;false表示只打断在等待中的任务)
     * @param ids
     * @return
     */
    public static Boolean[] stopTask(boolean mayInterruptIfRunning,Long ...ids){
        if(ids==null||ids.length==0){
            return new Boolean[0];
        }
        Boolean[] res=new Boolean[ids.length];
        for (int i=0;i<=ids.length-1;i++) {
            Long id=ids[i];
            Future future= CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.get(id);
            res[i]=future.cancel(mayInterruptIfRunning);
        }
        return res;
    }

}
