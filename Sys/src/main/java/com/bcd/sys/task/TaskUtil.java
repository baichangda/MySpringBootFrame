package com.bcd.sys.util;

import com.bcd.sys.task.SuperTaskBean;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.task.SysTaskRunnable;
import com.bcd.sys.task.TaskConsumer;
import com.bcd.sys.task.TaskRedisList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class TaskUtil {

    private final static Logger logger= LoggerFactory.getLogger(TaskUtil.class);

    @Autowired
    public void setRedisTemplate(@Qualifier(value = "taskRedisTemplate") RedisTemplate redisTemplate){
        //初始化系统任务线程池
        CommonConst.SYS_TASK_POOL=new ThreadPoolExecutor(2,2,30, TimeUnit.SECONDS,
                new TaskRedisList(CommonConst.SYS_TASK_LIST_NAME,redisTemplate));
    }

    @Bean(name = "taskRedisTemplate")
    public RedisTemplate taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        StringRedisSerializer keySerializer=new StringRedisSerializer();
        JdkSerializationRedisSerializer valueSerializer=new JdkSerializationRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 注册任务
     * @param supplier 提供任务实体
     * @param consumer
     * @param <T>
     * @return
     */
    public static <T extends SuperTaskBean>T registerTask(Supplier<T> supplier, TaskConsumer consumer){
        UserBean userBean=ShiroUtil.getCurrentUser();
        T taskBean=supplier.get();
        if(userBean!=null){
            taskBean.setCreateUserName(userBean.getUsername());
            taskBean.setCreateUserId(userBean.getId());
        }
        taskBean.setCreateTime(new Date());
        taskBean.setStatus(1);
        taskBean.setConsumer(consumer);
        taskBean.save();
        CommonConst.SYS_TASK_POOL.execute(new SysTaskRunnable(taskBean));
        return taskBean;
    }

    /**
     * 终止任务
     * @param taskBean
     * @param <T>
     * @return true代表终止成功;false代表终止失败(可能正在执行中或已经完成)
     */
    public static <T extends SuperTaskBean>boolean stopTask(T taskBean){
        boolean res=false;
        List<SysTaskRunnable> list= ((TaskRedisList)CommonConst.SYS_TASK_POOL.getQueue()).range(0,-1);
        for (SysTaskRunnable sysTaskRunnable : list) {
            if(sysTaskRunnable.getTaskBean().getId().equals(taskBean.getId())){
                res=CommonConst.SYS_TASK_POOL.getQueue().remove(sysTaskRunnable);
                break;
            }
        }
        if(res){
            taskBean.setStatus(3);
            taskBean.setFinishTime(new Date());
            taskBean.save();
            if(taskBean.getOnStop()!=null){
                try{
                    taskBean.onStop.accept(taskBean);
                }catch (Exception e){
                    logger.error("执行任务["+taskBean.getName()+"]的[onStop]出现异常",e);
                }
            }
        }
        return res;
    }

}
