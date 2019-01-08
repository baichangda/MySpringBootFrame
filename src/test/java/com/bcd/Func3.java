package com.bcd;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.function.NamedTaskFunction;
import org.springframework.stereotype.Component;

@Component
public class Func3 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func3";
    public Func3() {
        super(NAME);
    }

    @Override
    public TaskBean apply(TaskBean task) {
        throw BaseRuntimeException.getException("Func3发生错误");
    }
}
