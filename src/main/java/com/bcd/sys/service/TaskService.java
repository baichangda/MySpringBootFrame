package com.bcd.sys.service;

import com.bcd.base.support_jpa.service.BaseService;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.TaskDao;
import org.springframework.stereotype.Service;

import java.io.Serializable;

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
    public TaskBean doUpdate(TaskBean task) {
        return save(task);
    }

    @Override
    public void doDelete(TaskBean task) {
        delete(task);
    }
}
