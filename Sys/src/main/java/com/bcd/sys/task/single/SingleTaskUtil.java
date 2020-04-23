package com.bcd.sys.task.single;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.TaskRunnable;
import com.bcd.sys.task.TaskUtil;
import com.bcd.sys.task.TaskDAO;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class SingleTaskUtil {

    static Logger logger= LoggerFactory.getLogger(SingleTaskUtil.class);

    private static TaskDAO taskDAO;
    @Autowired
    public void init(TaskDAO taskDAO){
        SingleTaskUtil.taskDAO=taskDAO;
    }
    /**
     * 注册任务
     * @param task 任务
     * @param function 任务执行方法
     * @param <T> 任务泛型
     * @return
     */
    public static <T extends Task> Serializable registerTask(T task, TaskFunction<T> function){
        Serializable id;
        try {
            id =TaskUtil.onCreated(task);
        }catch (Exception e){
            throw BaseRuntimeException.getException(e);
        }
        TaskRunnable<T> taskRunnable=new TaskRunnable<>(task,function);
        CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.put(id.toString(),taskRunnable);
        CommonConst.SYS_TASK_POOL.execute(taskRunnable);
        return id;
    }

    /**
     * 停止任务
     * 只停止本机正在执行的任务
     * @param ids
     * @return
     */
    public static boolean[] stopTask(Serializable ...ids){
        if(ids==null||ids.length==0){
            return new boolean[0];
        }
        boolean []res=new boolean[ids.length];
        for(int i=0,end=res.length;i<end;i++) {
            TaskRunnable runnable = CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.get(ids[i].toString());
            res[i]=runnable.shutdown();
        }
        return res;
    }
}
