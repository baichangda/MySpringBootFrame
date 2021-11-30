package com.bcd.base.support_task;

public enum TaskStatus {
    WAITING(1, "等待中"),
    EXECUTING(2, "执行中"),
    SUCCEED(3, "执行成功"),
    FAILED(4, "执行失败"),

    CANCELED(5, "任务被取消"),
    STOPPED(6, "任务被终止");

    private final int status;
    private final String name;

    TaskStatus(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public static TaskStatus from(int status) {
        for (TaskStatus value : values()) {
            if (value.status == status) {
                return value;
            }
        }
        return null;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

}