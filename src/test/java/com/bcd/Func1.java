package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.NamedTaskFunction;
import com.bcd.sys.task.TaskContext;
import org.springframework.stereotype.Component;

@Component
public class Func1 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func1";


    @Override
    public void apply(TaskContext<TaskBean> context){
        try {
            Thread.sleep(10*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("func1 finish");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
