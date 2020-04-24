package com.bcd;

import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.service.RoleService;
import com.bcd.sys.task.NamedTaskFunction;
import com.bcd.sys.task.TaskContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Func4 extends NamedTaskFunction<TaskBean>{
    public final static String NAME="com.bcd.Func4";

    @Autowired
    RoleService roleService;

    @Override
    public void apply(TaskContext<TaskBean> context){
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RoleBean roleBean=new RoleBean();
        roleBean.setName("testFun4");
        roleBean.setCode("testFun4");
        roleService.save(roleBean);
        System.out.println("func4 finish");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
