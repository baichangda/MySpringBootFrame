package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.NamedTaskFunction;
import org.springframework.stereotype.Component;

@Component
public class Func1 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func1";
    public Func1() {
        super(NAME);
    }

    @Override
    public TaskBean apply(TaskBean task) throws InterruptedException{
        Thread.sleep(20*1000L);
        System.out.println(task.getParams()[0]);
        return task;
    }
}
