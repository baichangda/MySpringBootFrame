package com.bcd;

import com.bcd.sys.rdb.bean.TaskBean;
import com.bcd.sys.task.function.NamedTaskFunction;
import org.springframework.stereotype.Component;

@Component
public class Func4 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func4";
    public Func4() {
        super(NAME);
    }

    @Override
    public TaskBean apply(TaskBean task) {
        try {
            Thread.sleep(20*1000L);
            System.out.println(task.getParams()[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return task;
    }
}
