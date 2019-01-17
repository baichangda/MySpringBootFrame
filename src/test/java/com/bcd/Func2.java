package com.bcd;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.rdb.bean.OrgBean;
import com.bcd.sys.rdb.bean.TaskBean;
import com.bcd.sys.rdb.service.OrgService;
import com.bcd.sys.task.function.NamedTaskFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Func2 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func2";

    @Autowired
    OrgService orgService;
    public Func2() {
        super(NAME);
    }

    @Override
    @Transactional
    public TaskBean apply(TaskBean task) {
        try {
            OrgBean orgBean=new OrgBean();
            orgBean.setName("asdfasd");
            orgService.save(orgBean);
            Thread.sleep(10*1000L);
            System.out.println(task.getParams()[0]);
        } catch (InterruptedException e) {
            throw BaseRuntimeException.getException(e);
        }
        return task;
    }
}
