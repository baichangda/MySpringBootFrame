package com.bcd.sys.task;

import com.bcd.base.util.SpringUtil;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.service.TaskService;
import com.bcd.sys.util.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
@Component
public class TaskUtil {

    @Autowired
    public void init(@Qualifier(value = "string_jdk_redisTemplate") RedisTemplate redisTemplate){
        //初始化系统任务线程池
        CommonConst.SYS_TASK_POOL=new ThreadPoolExecutor(2,2,30, TimeUnit.SECONDS,
                new TaskRedisList(CommonConst.SYS_TASK_LIST_NAME,redisTemplate));
    }

    public static TaskService getTaskService(){
        return Init.taskService;
    }

    /**
     * 注册任务
     * @param name 任务名称
     * @param type 任务类型
     * @param consumer 任务执行方法
     * @param onStart 开始执行任务时回调
     * @param onSuccess 成功时回调
     * @param onFailed 失败时回调
     * @return
     */
    public static TaskBean registerTask(String name,int type,TaskConsumer consumer,TaskConsumer onStart,TaskConsumer onSuccess,TaskConsumer onFailed){
        UserBean userBean= ShiroUtil.getCurrentUser();
        TaskBean taskBean=new TaskBean(name,type);
        if(userBean!=null){
            taskBean.setCreateUserName(userBean.getUsername());
            taskBean.setCreateUserId(userBean.getId());
        }
        taskBean.setOnStart(onStart);
        taskBean.setOnSuccess(onSuccess);
        taskBean.setOnFailed(onFailed);
        taskBean.setCreateTime(new Date());
        taskBean.setStatus(1);
        taskBean.setConsumer(consumer);
        getTaskService().save(taskBean);
        CommonConst.SYS_TASK_POOL.execute(new SysTaskRunnable(taskBean));
        return taskBean;
    }

    public static TaskBean registerTask(String name,int type,TaskConsumer consumer){
        return registerTask(name, type, consumer,null,null,null);
    }

    /**
     * 终止任务
     * @param ids
     * @return true代表终止成功;false代表终止失败(可能正在执行中或已经完成)
     */
    public static boolean[] stopTask(Long ...ids){
        if(ids==null||ids.length==0){
            return new boolean[0];
        }
        List<SysTaskRunnable> list= ((TaskRedisList)CommonConst.SYS_TASK_POOL.getQueue()).range(0,-1);
        boolean[]res=new boolean[ids.length];
        List<Long> stopIdList=new ArrayList<>();
        for (int i=0;i<=ids.length-1;i++) {
            Long id=ids[i];
            for (SysTaskRunnable sysTaskRunnable : list) {
                if(sysTaskRunnable.getTaskBean().getId().equals(id)){
                    res[i]=CommonConst.SYS_TASK_POOL.getQueue().remove(sysTaskRunnable);
                    break;
                }
            }
            if(res[i]){
                stopIdList.add(id);
            }
        }
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("status",3);
        paramMap.put("ids",stopIdList);
        int count=new NamedParameterJdbcTemplate(Init.jdbcTemplate).update(
                "update t_sys_task set status=:status where id in (:ids)",paramMap);
        return res;
    }

    static class Init{
        private final static TaskService taskService=SpringUtil.applicationContext.getBean(TaskService.class);
        private final static JdbcTemplate jdbcTemplate=SpringUtil.applicationContext.getBean(JdbcTemplate.class);
    }

}
