package com.bcd.sys.task.single;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import com.bcd.sys.task.TaskUtil;
import com.bcd.sys.task.TaskDAO;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class SingleTaskUtil {
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
            id =TaskUtil.onCreate(task);
        }catch (Exception e){
            throw BaseRuntimeException.getException(e);
        }
        Future future= CommonConst.SYS_TASK_POOL.submit(new SysTaskRunnable(task,function,taskDAO));
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(id.toString(),future);
        return id;
    }

    /**
     * 停止任务
     * 只停止本机正在执行的任务
     * @param mayInterruptIfRunning
     * @param ids
     * @return
     */
    public static boolean[] stopTask(boolean mayInterruptIfRunning,Serializable ...ids){
        if(ids==null||ids.length==0){
            return new boolean[0];
        }
        boolean []res=new boolean[ids.length];
        if(mayInterruptIfRunning){
            List<Future> futureList= Arrays.stream(ids).map(id->CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.get(id.toString())).collect(Collectors.toList());
            //先移除队列中
            for(int i=0,end=futureList.size();i<end;i++) {
                Future future=futureList.get(i);
                res[i]=CommonConst.SYS_TASK_POOL.remove((FutureTask)future);
            }
            //检查所有的结果
            for(int i=0,end=res.length;i<end;i++){
                if(res[i]){
                    //从队列移除成功的触发更新回调
                    Task task = taskDAO.doRead(ids[i]);
                    TaskUtil.onStop(task);
                }else{
                    //不成功的进行强制移除
                    Future future=futureList.get(i);
                    future.cancel(true);
                }
            }
        }else{
            for(int i=0,end=res.length;i<end;i++) {
                Future future = CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.get(ids[i].toString());
                boolean curRes=future.cancel(false);
                res[i]=curRes;
            }
            //更新任务状态
            for(int i=0,end=res.length;i<end;i++){
                if(res[i]) {
                    //更新任务状态并触发停止回调
                    Task task = taskDAO.doRead(ids[i]);
                    TaskUtil.onStop(task);
                }
            }
        }
        return res;
    }
}
