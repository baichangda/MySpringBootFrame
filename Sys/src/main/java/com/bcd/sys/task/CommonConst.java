package com.bcd.sys.task;

import com.bcd.base.util.SpringUtil;
import com.bcd.sys.service.TaskService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CommonConst {
    /**
     * 用来存储任务对应的结果集(供关闭使用)
     */
    public final static ConcurrentHashMap<Long,Future> SYS_TASK_ID_TO_FUTURE_MAP=new ConcurrentHashMap<>();

    /**
     * 用来执行系统任务的线程池
     */
    public static ExecutorService SYS_TASK_POOL= Executors.newFixedThreadPool(2) ;

    public static class Init{
        public final static TaskService taskService= SpringUtil.applicationContext.getBean(TaskService.class);
        public final static JdbcTemplate jdbcTemplate=SpringUtil.applicationContext.getBean(JdbcTemplate.class);
        public final static RedisTemplate redisTemplate=(RedisTemplate)SpringUtil.applicationContext.getBean("string_jackson_redisTemplate");
    }
}
