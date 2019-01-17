package com.bcd.base.cache;

import com.bcd.base.util.JsonUtil;

import java.io.Serializable;

public class MySimpleKey implements Serializable{
    private String className;
    private String methodName;
    private Object[] args;

    public MySimpleKey(String className, String methodName, Object... args) {
        this.className = className;
        this.methodName = methodName;
        this.args = args;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
