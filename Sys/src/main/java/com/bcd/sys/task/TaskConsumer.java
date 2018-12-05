package com.bcd.sys.task;

import com.bcd.sys.bean.TaskBean;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 所有此接口的实现类 里面 如果包含成员变量
 * 成员变量必须实现序列化接口
 */
public interface TaskConsumer extends Serializable{
    void accept(TaskBean taskBean) throws InterruptedException ;
}
