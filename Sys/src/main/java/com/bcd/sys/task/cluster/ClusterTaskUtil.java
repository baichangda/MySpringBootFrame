package com.bcd.sys.task.cluster;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.Task;
import com.bcd.sys.task.TaskUtil;
import com.bcd.sys.task.TaskDAO;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
@Component
public class ClusterTaskUtil {
    private final static Logger logger= LoggerFactory.getLogger(ClusterTaskUtil.class);

    /**
     * 集群模式中redis任务队列名称
     */
    public final static String SYS_TASK_LIST_NAME="sysTask";


    /**
     * 用来接收 停止系统任务 通道名
     */
    public final static String STOP_SYS_TASK_CHANNEL="stopSysTaskChannel";

    /**
     * 用来接收 停止系统任务结果 通道名
     */
    public final static String STOP_SYS_TASK_RESULT_CHANNEL="stopSysTaskResultChannel";

    /**
     * 用来唤醒请求线程
     * key: 停止任务请求的id
     * value: 当前停止请求上下文
     */
    public final static ConcurrentHashMap<String, StopSysTaskContext> STOP_SYS_TASK_CODE_TO_CONTEXT_MAP =new ConcurrentHashMap<>();


    private static SysTaskRedisQueue sysTaskRedisQueue;

    private static StopSysTaskListener stopSysTaskListener;

    private static TaskDAO taskDAO;

    @Autowired
    public void init(SysTaskRedisQueue sysTaskRedisQueue,StopSysTaskListener stopSysTaskListener,TaskDAO taskDAO){
        ClusterTaskUtil.sysTaskRedisQueue=sysTaskRedisQueue;
        ClusterTaskUtil.stopSysTaskListener=stopSysTaskListener;
        ClusterTaskUtil.taskDAO=taskDAO;
    }
    /**
     * 注册任务(redis队列任务模式)
     * @param task 任务
     * @param functionName 任务执行方法名称
     * @param params 任务执行参数
     * @param <T> 任务泛型
     * @return
     */
    public static <T extends ClusterTask>Serializable registerTask(T task, String functionName, Object ... params){
        Serializable id;
        try {
            task.setFunctionName(functionName);
            task.setParams(params);
            id=TaskUtil.onCreated(task);
            sysTaskRedisQueue.send(task);
        }catch (Exception e){
            throw BaseRuntimeException.getException(e);
        }
        return id;
    }
    /**
     * 终止任务(redis队列任务模式)
     * @param ids
     * @return
     */
    public static Boolean[] stopTask(Serializable...ids){
        //先移除队列中正在等待的任务
        LinkedHashMap<Serializable,Boolean> resMap=sysTaskRedisQueue.remove(ids);
        //先执行从队列中移除任务的回调
        resMap.forEach((k,v)->{
            if(v){
                Task task= taskDAO.doRead(k);
                TaskUtil.onCanceled(task);
            }
        });
        //更新终止成功任务的状态,同时记录终止失败任务id并准备进行运行任务打断
        List<Serializable> failedIdList = new LinkedList<>();
        resMap.forEach((k, v) -> {
            if (!v) {
                failedIdList.add(k);
            }
        });
        //如果没有移除失败的任务,则直接返回结果;否则进行运行任务打断
        if (!failedIdList.isEmpty()) {
            //通过redis通知其他服务器执行
            String code = RandomStringUtils.randomAlphabetic(32);
            StopSysTask stopSysTask = new StopSysTask();
            String[] idArr = failedIdList.stream().map(Object::toString).toArray(String[]::new);
            stopSysTask.setIds(idArr);
            stopSysTask.setCode(code);
            //定义结果
            StopSysTaskContext stopSysTaskContext = new StopSysTaskContext();
            stopSysTaskContext.setCode(code);
            stopSysTaskContext.setIds(idArr);
            ClusterTaskUtil.STOP_SYS_TASK_CODE_TO_CONTEXT_MAP.put(code, stopSysTaskContext);
            //发送通知
            stopSysTaskListener.send(stopSysTask);
            //主线程等待结果
            synchronized (stopSysTaskContext) {
                try {
                    long t1 = System.currentTimeMillis();
                    long timeout = 30 * 1000;
                    while (true) {
                        stopSysTaskContext.wait(timeout);
                        //每次唤醒后检查是否所有结果都处理了
                        if (idArr.length == stopSysTaskContext.getResult().size()) {
                            break;
                        }
                        //检查是否超时
                        timeout -= (System.currentTimeMillis() - t1);
                        if (timeout <= 0) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    throw BaseRuntimeException.getException(e);
                }
            }
            //处理结果
            Map<String, Boolean> curResMap = stopSysTaskContext.getResult();
            for (int i = 0, end = idArr.length; i < end; i++) {
                Boolean curRes = curResMap.get(idArr[i]);
                if (curRes!=null&&curRes) {
                    resMap.put(failedIdList.get(i), true);
                }
            }
        }
        return resMap.values().toArray(new Boolean[0]);
    }


}
