package com.bcd.sys.service;

import com.bcd.base.support_jdbc.service.BaseService;
import com.bcd.sys.bean.TaskBean;
import com.bcd.base.support_task.TaskDao;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TaskService extends BaseService<Long,TaskBean> implements TaskDao<TaskBean, Long> {
    @Override
    public TaskBean doCreate(TaskBean task) {
        insert(task);
        return task;
    }

    @Override
    public TaskBean doRead(Long id) {
        return get(id);
    }

    @Override
    public void doUpdate(TaskBean task) {
        update(task);
    }

    @Override
    public void doDelete(TaskBean task) {
        delete(task.getId());
    }
}
