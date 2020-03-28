package com.bcd.sys.task.function;

import com.bcd.sys.task.entity.Task;

public abstract class NamedTaskFunction<T extends Task> implements TaskFunction<T>{
    protected String name;

    protected NamedTaskFunction(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

}
