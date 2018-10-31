package com.bcd.sys.task;

import com.bcd.sys.bean.TaskBean;

import java.io.Serializable;
import java.util.function.Consumer;

public interface TaskConsumer extends Consumer<TaskBean>,Serializable{

}
