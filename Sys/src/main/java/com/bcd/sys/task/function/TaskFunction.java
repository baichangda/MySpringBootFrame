package com.bcd.sys.task.function;

import com.bcd.sys.task.entity.Task;

public interface TaskFunction<T extends Task> {
      T apply(T task);
}
