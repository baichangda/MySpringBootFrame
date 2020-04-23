package com.bcd;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.NamedTaskFunction;
import org.springframework.stereotype.Component;

@Component
public class Func5 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func5";
    public Func5() {
        super(NAME);
    }

    @Override
    public void apply(TaskBean task){
        try {
            Thread.sleep(10*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("func5 finish");
    }
}
