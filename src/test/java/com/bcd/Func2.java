package com.bcd;

import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.RoleService;
import com.bcd.sys.task.NamedTaskFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Func2 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func2";

    @Autowired
    RoleService roleService;

    public Func2() {
        super(NAME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(TaskBean task){
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RoleBean roleBean=new RoleBean();
        roleBean.setName("testFun2");
        roleBean.setCode("testFun2");
        roleService.save(roleBean);
        System.out.println("func2 finish");
    }
}
