package com.bcd.sys.task;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.dao.TaskDAO;
import com.bcd.sys.task.entity.ClusterTask;
import com.bcd.sys.task.entity.Task;
import com.bcd.sys.task.function.TaskFunction;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil {

    private final static Logger logger= LoggerFactory.getLogger(TaskUtil.class);

    private static StopSysTaskListener stopSysTaskListener;

    private static SysTaskRedisQueue sysTaskRedisQueue;

    private static TaskDAO taskDAO;

    @Autowired
    public void init(StopSysTaskListener stopSysTaskListener,SysTaskRedisQueue sysTaskRedisQueue,TaskDAO taskDAO){
        TaskUtil.stopSysTaskListener=stopSysTaskListener;
        TaskUtil.sysTaskRedisQueue=sysTaskRedisQueue;
        TaskUtil.taskDAO=taskDAO;
    }

    /**
     * 注册任务(redis队列任务模式)
     * @param task 任务
     * @param functionName 任务执行方法名称
     * @param params 任务执行参数
     * @param <T> 任务泛型
     * @return
     */
    public static <T extends ClusterTask>Serializable registerClusterTask(T task, String functionName, Object ... params){
        Serializable id;
        try {
            task.setFunctionName(functionName);
            task.setParams(params);
            task.onCreate();
            id=taskDAO.doCreate(task);
        }catch (Exception e){
            throw BaseRuntimeException.getException(e);
        }
        sysTaskRedisQueue.send(task);
        return id;
    }

    /**
     * 注册任务
     * @param task 任务
     * @param function 任务执行方法
     * @param <T> 任务泛型
     * @return
     */
    public static <T extends Task>Serializable registerTask(T task, TaskFunction<T> function){
        Serializable id;
        try {
            task.onCreate();
            id=taskDAO.doCreate(task);
        }catch (Exception e){
            throw BaseRuntimeException.getException(e);
        }
        Future future= CommonConst.SYS_TASK_POOL.submit(new SysTaskRunnable(task,function,taskDAO));
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(id,future);
        return id;
    }

    /**
     * 终止任务(无论是正在执行中还是等待中)
     * 原理:
     * 1、将id通过redis channel推送到各个服务器
     * 2、各个服务器获取到要终止的任务id,检查是否在当前服务器中正在执行此任务
     * 3、通过调用Future cancel()方法来终止正在执行的任务,并将通过redis channel推送到发起请求的服务器
     * 4、接收到结果后,由请求服务器更新任务状态到 TaskStatus.STOPPED
     *
     * 注意:
     * 如果是执行中的任务被结束,虽然已经调用Future cancel()但是并不会马上结束,具体原理参考Thread interrupt()
     *
     * @param mayInterruptIfRunning 是否打断正在运行的任务(true表示打断wait或者sleep的任务;false表示只打断在等待中的任务)
     * @param ids
     *
     * @return 结果数组;true代表终止成功;false代表终止失败(可能已经取消或已经完成)
     */
    public static Boolean[] stopTask(boolean mayInterruptIfRunning,Serializable ...ids){
        if(ids==null||ids.length==0){
            return new Boolean[0];
        }
        //1、生成当前停止任务请求随机编码
        String code= RandomStringUtils.randomAlphanumeric(32);
        //2、构造当前请求的空结果集并加入到全局map
        ConcurrentHashMap<Serializable,Boolean> resultMap=new ConcurrentHashMap<>();
        CommonConst.SYS_TASK_CODE_TO_RESULT_MAP.put(code,resultMap);
        //3、锁住此次请求的结果map,等待,便于本服务器其他线程收到结果时唤醒
        //3.1、定义退出循环标记
        boolean isFinish=false;
        synchronized (resultMap){
            //3.2、构造请求数据,推送给其他服务器停止任务
            Map<String,Object> dataMap=new HashMap<>();
            dataMap.put("code",code);
            dataMap.put("ids",ids);
            dataMap.put("mayInterruptIfRunning",mayInterruptIfRunning);
            stopSysTaskListener.send(dataMap);
            try {
                //3.3、设置任务等待超时时间,默认为30s,如果在规定时间内还没有收到所有服务器通知,就不进行等待了,主要是为了解决死循环问题
                long t=30*1000L;
                while(!isFinish&&t>0){
                    long startTs=System.currentTimeMillis();
                    //3.4、等待其他线程唤醒
                    resultMap.wait(t);
                    //3.5、唤醒后,如果检测到接收到的结果集与请求停止任务数量相等,则表示结果已经完整,结束循环
                    if(resultMap.size()==ids.length){
                        isFinish=true;
                    }else{
                        t-=(System.currentTimeMillis()-startTs);
                    }
                }

                //3.6、停止成功的任务更新其任务状态
                resultMap.forEach((k,v)->{
                    if(v){
                        Task task= taskDAO.doRead(k);
                        try {
                            task.onStop();
                            taskDAO.doUpdate(task);
                        }catch (Exception e){
                            logger.error("Task["+k+"] Execute onStop Error",e);
                        }
                    }
                });
                //3.7、根据返回的数据构造结果集(结果集不一定准确,因为有可能在规定时间之内没有收到结果,会判定为终止失败)
                return Arrays.stream(ids).map(id->resultMap.getOrDefault(id,false)).toArray(len->new Boolean[len]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 终止任务(redis队列任务模式)
     * @param mayInterruptIfRunning
     * @param ids
     * @return
     */
    public static Boolean[] stopClusterTask(boolean mayInterruptIfRunning, Serializable ...ids){
        //1、先移除队列中正在等待的任务
        LinkedHashMap<Serializable,Boolean> resMap=sysTaskRedisQueue.remove(ids);
        //2、如果参数为不打断正在运行的任务,则返回;否则打断每个集群实例正在执行的任务
        if(mayInterruptIfRunning){
            //3、更新终止成功任务的状态,同时记录终止失败任务id并准备进行运行任务打断
            List<Serializable> failedIdList=new ArrayList<>();
            resMap.forEach((k,v)->{
                if(v){
                    Task task= taskDAO.doRead(k);
                    try {
                        task.onStop();
                        taskDAO.doUpdate(task);
                    }catch (Exception e){
                        logger.error("Task["+k+"] Execute onStop Error",e);
                    }
                }else{
                    failedIdList.add(k);
                }
            });
            //4、如果没有失败的任务,则直接返回结果;否则进行运行任务打断
            if(failedIdList.isEmpty()){
                return resMap.values().stream().toArray(len->new Boolean[len]);
            }else{
                //5、获取正在运行任务打断结果,将结果合并到resMap中
                Boolean[] executingResArr=stopTask(true,failedIdList.stream().toArray(len->new Serializable[len]));
                for(int i=0;i<=executingResArr.length-1;i++){
                    if(executingResArr[i]){
                        resMap.put(failedIdList.get(i),true);
                    }
                }
                return resMap.values().stream().toArray(len->new Boolean[len]);
            }
        }else{
            //6、如果是只移除等待中的任务,则更新任务状态,返回结果
            resMap.forEach((k,v)->{
                if(v){
                    Task task= taskDAO.doRead(k);
                    try {
                        task.onStop();
                        taskDAO.doUpdate(task);
                    }catch (Exception e){
                        logger.error("Task["+k+"] Execute onStop Error",e);
                    }
                }
            });
            return resMap.values().stream().toArray(len->new Boolean[len]);
        }
    }
}
