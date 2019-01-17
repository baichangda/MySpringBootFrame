package com.bcd.sys.mongodb.service;

import com.bcd.mongodb.service.BaseService;
import com.bcd.sys.task.dao.TaskDAO;
import com.bcd.sys.task.entity.Task;
import org.springframework.stereotype.Service;
import com.bcd.sys.mongodb.bean.TaskBean;

import java.io.Serializable;

/**
 *
 */
//@Service
public class TaskService extends BaseService<TaskBean,String> implements TaskDAO<String,TaskBean>{
    @Override
    public Serializable doCreate(TaskBean task) {
        return save(task);
    }

    @Override
    public Task doRead(String id) {
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
