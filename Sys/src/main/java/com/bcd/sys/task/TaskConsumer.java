package com.bcd.sys.task;

import com.bcd.sys.bean.TaskBean;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 所有此接口的实现类 里面 如果包含成员变量
 * 成员变量必须实现序列化接口
 */
public abstract class TaskConsumer{
    protected String name;

    public TaskConsumer(String name) {
        this.name=name;
        CommonConst.NAME_TO_CONSUMER_MAP.put(name,this);
    }

    public String getName() {
        return name;
    }

    public abstract void accept(TaskBean taskBean) throws InterruptedException ;
}
