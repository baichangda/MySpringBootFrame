package com.bcd.sys.task.cluster;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.task.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import com.bcd.sys.task.TaskConsumer;
import com.bcd.sys.task.TaskStatus;
import com.bcd.sys.util.ShiroUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil {

    @Autowired
    public void init(@Qualifier(value = "string_jdk_redisTemplate") RedisTemplate redisTemplate){
        //初始化系统任务线程池
        TaskConst.SYS_TASK_POOL=new ThreadPoolExecutor(2,2,30, TimeUnit.SECONDS,
                new TaskRedisList(TaskConst.SYS_TASK_LIST_NAME,redisTemplate));
    }

    /**
     * 注册任务
     * @param name 任务名称
     * @param type 任务类型 (对应TaskBean里面的type,根据不同的项目翻译成不同的意思,默认 1:普通任务;2:文件类型任务 )
     * @param consumer 任务执行方法
     * @return
     */
    public static TaskBean registerTask(String name,int type,TaskConsumer consumer){
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
     * 终止正在等待执行的任务
     * 原理:
     * 通过移除调redis任务队列中的任务来实现
     * @param ids
     * @return 结果数组;true代表终止成功;false代表终止失败(可能正在执行中或已经完成)
     */
    public static Boolean[] stopTaskInWaiting(Long ...ids){
        if(ids==null||ids.length==0){
            return new Boolean[0];
        }
        List<SysTaskRunnable> list= ((TaskRedisList) TaskConst.SYS_TASK_POOL.getQueue()).range(0,-1);
        Boolean[]res=new Boolean[ids.length];
        List<Long> stopIdList=new ArrayList<>();
        for (int i=0;i<=ids.length-1;i++) {
            Long id=ids[i];
            for (SysTaskRunnable sysTaskRunnable : list) {
                if(sysTaskRunnable.getTaskBean().getId().equals(id)){
                    res[i]= TaskConst.SYS_TASK_POOL.getQueue().remove(sysTaskRunnable);
                    break;
                }
            }
            if(res[i]){
                stopIdList.add(id);
            }
        }
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("status",TaskStatus.STOPPED.getStatus());
        paramMap.put("ids",stopIdList);
        int count=new NamedParameterJdbcTemplate(CommonConst.Init.jdbcTemplate).update(
                "update t_sys_task set status=:status,finish_time=now() where id in (:ids)",paramMap);
        return res;
    }

    /**
     * 终止正在等待执行的所有任务
     * 原理:
     * 通过移除调redis任务队列中的任务来实现
     * @return 停止的所有任务的id数组
     */
    public static Long[] stopAllTaskInWaiting(){
        List<Runnable> list= new ArrayList<>();
        TaskConst.SYS_TASK_POOL.getQueue().drainTo(list);
        List<Long> idList= list.stream().map(e->((SysTaskRunnable)e).getTaskBean().getId()).collect(Collectors.toList());
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("status",TaskStatus.STOPPED.getStatus());
        paramMap.put("ids",idList);
        int count=new NamedParameterJdbcTemplate(CommonConst.Init.jdbcTemplate).update(
                "update t_sys_task set status=:status,finish_time=now() where id in (:ids)",paramMap);
        return list.stream().toArray(len->new Long[len]);
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
     * @return 结果数组;true代表终止成功;false代表终止失败(可能已经取消或已经完成)
     */
    public static Boolean[] stopTask(Long ...ids){
        if(ids==null||ids.length==0){
            return new Boolean[0];
        }
        //1、生成当前停止任务请求随机编码
        String code= RandomStringUtils.randomAlphanumeric(32);
        //2、构造当前请求的空结果集并加入到全局map
        ConcurrentHashMap<Long,Boolean> resultMap=new ConcurrentHashMap<>();
        TaskConst.SYS_TASK_CODE_TO_RESULT_MAP.put(code,resultMap);
        //3、锁住此次请求的结果map,等待,便于本服务器其他线程收到结果时唤醒
        //3.1、定义退出循环标记
        boolean isFinish=false;
        synchronized (resultMap){
            //3.2、构造请求数据,推送给其他服务器停止任务
            Map<String,Object> dataMap=new HashMap<>();
            dataMap.put("code",code);
            dataMap.put("ids",ids);
            CommonConst.Init.redisTemplate.convertAndSend(TaskConst.STOP_SYS_TASK_CHANNEL,dataMap);
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
                //3.6、根据返回的数据构造结果集(结果集不一定准确,因为有可能在规定时间之内没有收到结果,会判定为终止失败)
                return Arrays.stream(ids).map(id->resultMap.getOrDefault(id,false)).toArray(len->new Boolean[len]);
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }
}
