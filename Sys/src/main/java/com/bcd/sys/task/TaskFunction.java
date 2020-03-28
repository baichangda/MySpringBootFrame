package com.bcd.sys.task;

public interface TaskFunction<T extends Task> {
      T apply(T task) throws InterruptedException;
}
