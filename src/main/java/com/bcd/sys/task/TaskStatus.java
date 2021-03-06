package com.bcd.sys.task;

public enum TaskStatus {
    WAITING(1, "等待中"),
    EXECUTING(2, "执行中"),
    SUCCEED(3, "执行成功"),
    FAILED(4, "执行失败"),

    CANCELED(5, "任务被取消"),
    STOPPING(6, "任务停止中"),
    STOPPED(7, "任务被终止"),

    EXCEPTION(8, "任务执行异常");

    private int status;
    private String name;

    TaskStatus(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}