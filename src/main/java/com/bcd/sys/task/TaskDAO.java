package com.bcd.sys.task;

import java.io.Serializable;

public interface TaskDAO<K extends Serializable, T extends Task> {
    /**
     * 创建task
     *
     * @param task
     * @return
     */
    Serializable doCreate(T task);

    /**
     * 根据id读取task
     *
     * @param id
     * @return
     */
    T doRead(K id);

    /**
     * 更新task
     *
     * @param task
     */
    void doUpdate(T task);

    /**
     * 删除task
     *
     * @param task
     */
    void doDelete(T task);
}
