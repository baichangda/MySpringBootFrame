package com.bcd.sys.task.cluster;

import com.bcd.sys.task.Task;

public interface ClusterTask extends Task {
    /**
     * 获取方法名称
     * @return
     */
    String getFunctionName();

    void setFunctionName(String functionName);

    /**
     * 获取参数
     * @return
     */
    Object[] getParams();

    void setParams(Object[] params);
}