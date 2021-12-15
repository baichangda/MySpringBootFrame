package com.bcd.base.support_task;

import java.io.Serializable;

public interface TaskDao<T extends Task<K>,K extends Serializable> {
    /**
     * 创建task
     *
     * @param task
     * @return
     */
    T doCreate(T task);

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
