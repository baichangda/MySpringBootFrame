package com.bcd.sys.util;

import com.bcd.sys.bean.TaskBean;
import com.bcd.sys.bean.UserBean;

import java.util.Date;

public class TaskUtil {
    public static TaskBean registerCommonTask(String name){
        UserBean userBean=ShiroUtil.getCurrentUser();
        TaskBean taskBean=new TaskBean();
        taskBean.setName(name);
        taskBean.setCreateUserName(userBean.getUsername());
        taskBean.setCreateUserId(userBean.getId());
        taskBean.setCreateTime(new Date());
        taskBean.setCreateIp(IPUtil.getIpAddress());
        taskBean.setStatus(1);
        return null;
    }

    public static TaskBean registerFileTask(String name){
        UserBean userBean=ShiroUtil.getCurrentUser();
        TaskBean taskBean=new TaskBean();
        taskBean.setName(name);
        taskBean.setCreateUserName(userBean.getUsername());
        taskBean.setCreateUserId(userBean.getId());
        taskBean.setCreateTime(new Date());
        taskBean.setCreateIp(IPUtil.getIpAddress());
        taskBean.setStatus(2);
        return null;
    }
}
