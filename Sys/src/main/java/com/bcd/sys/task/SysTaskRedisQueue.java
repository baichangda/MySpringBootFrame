package com.bcd.sys.task;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.dao.TaskDAO;
import com.bcd.sys.task.entity.ClusterTask;
import com.bcd.sys.task.function.NamedTaskFunction;
import com.bcd.sys.task.function.TaskFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class SysTaskRedisQueue<T extends ClusterTask> implements ApplicationListener<ContextRefreshedEvent>{
    private final static Logger logger= LoggerFactory.getLogger(SysTaskRedisQueue.class);
    private String name;
    private Map<String,NamedTaskFunction<T>> taskFunctionMap;

    private Semaphore lock=new Semaphore(CommonConst.SYS_TASK_POOL.getMaximumPoolSize());

    private BoundListOperations boundListOperations;

    private long popIntervalMills;

    @Autowired
    private TaskDAO taskDAO;

    public SysTaskRedisQueue(@Qualifier("string_serializable_redisTemplate")RedisTemplate redisTemplate) {
        this.name=CommonConst.SYS_TASK_LIST_NAME;
        this.boundListOperations=redisTemplate.boundListOps(CommonConst.SYS_TASK_LIST_NAME);
        this.popIntervalMills =((LettuceConnectionFactory)redisTemplate.getConnectionFactory()).getTimeout()/2;
    }

    /**
     * 从redis list中获取任务并执行
     * @throws InterruptedException
     */
    private void fetchAndExecute() throws InterruptedException {
        lock.acquire();
        Object data = boundListOperations.rightPop(popIntervalMills, TimeUnit.MILLISECONDS);
        if (data == null) {
            lock.release();
        }else{
            Worker.WORK_POOL.execute(() -> {
                try {
                    onTask(data);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    lock.release();
                }
            });
        }
    }

    /**
     * 接收到任务处理
     * @param data
     */
    public void onTask(Object data) {
        //1、接收并解析任务数据
        T task=(T) data;
        Serializable id=task.getId();
        String functionName=task.getFunctionName();
        TaskFunction<T> taskFunction= taskFunctionMap.get(functionName);
        //2、如果找不到对应执行方法实体,则任务执行失败并抛出异常
        if(taskFunction==null){
            BaseRuntimeException exception= BaseRuntimeException.getException("Can't Find ClusterTask["+functionName+"],Please Check It");
            task.onFailed(exception);
            taskDAO.doUpdate(task);
            throw exception;
        }
        //3、使用线程池执行任务
        Future future= CommonConst.SYS_TASK_POOL.submit(()->{
            //3.1、执行任务
            new SysTaskRunnable(task,taskFunction,taskDAO).run();
            //3.2、执行完毕后释放锁
            lock.release();
        });
        CommonConst.SYS_TASK_ID_TO_FUTURE_MAP.put(id,future);
    }

    public void send(T task) {
        boundListOperations.leftPush(task);
    }

    public LinkedHashMap<Serializable,Boolean> remove(Serializable ... ids) {
        if(ids==null||ids.length==0){
            return new LinkedHashMap<>();
        }
        LinkedHashMap<Serializable,Boolean> resMap=new LinkedHashMap<>();
        List<T> dataList= boundListOperations.range(0L,-1L);
        for (Serializable id : ids) {
            boolean res=false;
            for (T task : dataList) {
                if(id.equals(task.getId())){
                    Long count=boundListOperations.remove(1,task);
                    if(count!=null&&count==1){
                        res=true;
                        break;
                    }else{
                        res=false;
                        break;
                    }
                }
            }
            resMap.put(id,res);
        }
        return resMap;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        taskFunctionMap=contextRefreshedEvent.getApplicationContext().getBeansOfType(NamedTaskFunction.class).values().stream().collect(Collectors.toMap(NamedTaskFunction::getName, e->e,(e1, e2)->e1));
        Worker.init(this);
    }

    static class Worker{
        /**
         * 从redis中遍历数据的线程池
         */
        private final static ExecutorService POOL= Executors.newSingleThreadExecutor();

        /**
         * 执行工作任务的线程池
         */
        private final static ExecutorService WORK_POOL=Executors.newCachedThreadPool();

        public static void init(SysTaskRedisQueue sysTaskRedisQueue){
            POOL.execute(()->{
                while(true){
                    try {
                        sysTaskRedisQueue.fetchAndExecute();
                    } catch (Exception e) {
                        logger.error("SysTaskRedisQueue["+sysTaskRedisQueue.name+"] Stop", e);
                        break;
                    }
                }
            });
        }
    }
}
