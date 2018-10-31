package com.bcd.sys.task;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class TaskRedisList extends DefaultRedisList{
    private long timeout;
    public TaskRedisList(String key, RedisOperations operations) {
        super(key, operations);
        this.timeout=((LettuceConnectionFactory)(((RedisTemplate)operations).getConnectionFactory())).getClientConfiguration().getCommandTimeout().getSeconds();
    }

    /**
     * 重写此方法的原因是因为redis连接有连接超时时间,如果阻塞式的读取值,会抛出连接超时异常;必须以超时时间1/2的时间来循环读取
     * @return
     * @throws InterruptedException
     */
    @Nullable
    @Override
    public Object take() throws InterruptedException {
        try {
            Object res;
            while ((res = poll(timeout / 2, TimeUnit.SECONDS)) == null) {

            }
            return res;
        }catch (Exception e){
//            e.printStackTrace();
            return null;
        }
    }


    /**
     * 重写此方法的原因是因为在任务完成以后,如果调用shutdown,java线程池会打断线程执行;此时会造成正在等待redis结果的连接被打断
     * 出现
     * @return
     */
    @Override
    public int size() {
        try {
            return super.size();
        }catch (Exception e){
//            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Iterator iterator() {
        return super.iterator();
    }
}
