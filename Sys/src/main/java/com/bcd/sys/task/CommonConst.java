package com.bcd.sys.task;

import com.bcd.base.util.SpringUtil;
import com.bcd.sys.service.TaskService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CommonConst {
    /**
     * 用来存储任务对应的结果集(供关闭使用)
     */
    public final static ConcurrentHashMap<Long,Future> SYS_TASK_ID_TO_FUTURE_MAP=new ConcurrentHashMap<>();

    /**
     * 用来执行系统任务的线程池
     */
    public static ThreadPoolExecutor SYS_TASK_POOL= (ThreadPoolExecutor)Executors.newFixedThreadPool(2) ;

    public static class Init{
        public final static TaskService taskService= SpringUtil.applicationContext.getBean(TaskService.class);
        public final static JdbcTemplate jdbcTemplate=SpringUtil.applicationContext.getBean(JdbcTemplate.class);
        public final static RedisTemplate redisTemplate=(RedisTemplate)SpringUtil.applicationContext.getBean("string_jackson_redisTemplate");
    }


    /**
     * 定义任务处理器 名称和实体对应关系,name可以供存储,执行时候通过此map找到实体然后执行
     */
    public final static Map<String,TaskConsumer> NAME_TO_CONSUMER_MAP=new ConcurrentHashMap<>();
}
