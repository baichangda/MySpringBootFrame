package com.bcd.sys.task.entity;

public interface ClusterSupport {
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
