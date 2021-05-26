package com.bcd.sys.task.single;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Component
public class SingleTaskUtil {

    static Logger logger = LoggerFactory.getLogger(SingleTaskUtil.class);

    /**
     * 注册任务
     *
     * @param context 任务上下文环境
     * @param <T>     任务泛型
     * @return
     */
    public static <T extends Task> Serializable registerTask(TaskContext<T> context) {
        Serializable id;
        try {
            T task = context.getTask();
            id = TaskUtil.onCreated(task);
        } catch (Exception e) {
            throw BaseRuntimeException.getException(e);
        }
        TaskRunnable<T> taskRunnable = new TaskRunnable<>(context);
        CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.put(id.toString(), taskRunnable);
        CommonConst.SYS_TASK_POOL.execute(taskRunnable);
        return id;
    }

    /**
     * 停止任务
     * 只停止本机正在执行的任务
     *
     * @param ids
     * @return
     */
    public static boolean[] stopTask(Serializable... ids) {
        if (ids == null || ids.length == 0) {
            return new boolean[0];
        }
        boolean[] res = new boolean[ids.length];
        for (int i = 0, end = res.length; i < end; i++) {
            TaskRunnable runnable = CommonConst.SYS_TASK_ID_TO_TASK_RUNNABLE_MAP.get(ids[i].toString());
            res[i] = runnable.shutdown();
        }
        return res;
    }
}
