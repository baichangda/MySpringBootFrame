package com.bcd;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.task.NamedTaskFunction;
import com.bcd.sys.task.TaskContext;
import org.springframework.stereotype.Component;

@Component
public class Func3 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func3";

    @Override
    public void apply(TaskContext<TaskBean> context){
        throw BaseRuntimeException.getException("Func3发生错误");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
