package com.bcd.sys.task;

import com.bcd.sys.bean.TaskBean;

public interface TaskExecutor {
    void execute(TaskBean taskBean);
}
