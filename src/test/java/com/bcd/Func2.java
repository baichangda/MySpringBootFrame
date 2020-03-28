package com.bcd;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.sys.bean.OrgBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.OrgService;
import com.bcd.sys.task.NamedTaskFunction;
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
    @Transactional(rollbackFor = Exception.class)
    public TaskBean apply(TaskBean task)  throws InterruptedException{
        OrgBean orgBean=new OrgBean();
        orgBean.setName("testFun2");
        orgBean.setCode("testFun2");
        orgService.save(orgBean);
        try {
            Thread.sleep(10 * 1000L);
        }catch (InterruptedException ex){
            System.out.println("==============Func2被打断");
            throw ex;
        }
        System.out.println(task.getParams()[0]);
        return task;
    }
}
