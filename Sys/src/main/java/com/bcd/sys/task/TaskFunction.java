package com.bcd.sys.task;

public interface TaskFunction<T extends Task> {
      void apply(T task);

      default boolean supportShutdown(T task){
            return false;
      }

      default void shutdown(T task){
            //do nothing
      }
}
