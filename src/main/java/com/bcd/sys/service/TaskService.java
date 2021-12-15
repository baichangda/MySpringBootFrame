package com.bcd.sys.service;

import com.bcd.base.support_jpa.service.BaseService;
import com.bcd.sys.bean.TaskBean;
import com.bcd.base.support_task.TaskDao;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TaskService extends BaseService<TaskBean, Long> implements TaskDao<TaskBean,Long> {
    @Override
    public TaskBean doCreate(TaskBean task) {
        return save(task);
    }

    @Override
    public TaskBean doRead(Long id) {
        return findById(id);
    }

    @Override
    public void doUpdate(TaskBean task) {
        save(task);
    }

    @Override
    public void doDelete(TaskBean task) {
        delete(task);
    }
}
