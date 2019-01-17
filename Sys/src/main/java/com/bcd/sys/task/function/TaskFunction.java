package com.bcd.sys.rdb.task.function;

import com.bcd.sys.rdb.task.entity.Task;

public interface TaskFunction<T extends Task> {
      T apply(T task);
}
