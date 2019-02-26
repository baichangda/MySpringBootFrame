package com.bcd.sys.service;

import com.bcd.rdb.service.BaseService;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.dao.TaskDAO;
import com.bcd.sys.task.entity.Task;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 *
 */
@Service
public class TaskService extends BaseService<TaskBean,Long> implements TaskDAO<Long,TaskBean>{
    @Override
    public Serializable doCreate(TaskBean task) {
        return save(task).getId();
    }

    @Override
    public Task doRead(Long id) {
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
