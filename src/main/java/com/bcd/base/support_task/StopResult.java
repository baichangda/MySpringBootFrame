package com.bcd.base.support_task;

public enum StopResult {
    CANCEL_SUCCEED(1, "任务取消成功"),
    IN_EXECUTING_INTERRUPT_NOT_SUPPORT(2, "任务运行中、支持打断"),
    IN_EXECUTING_INTERRUPT_SUCCEED(3, "任务运行中、已请求打断、等待停止"),
    EXCEPTION(4, "任务异常、停止失败"),
    ;

    private final int flag;
    private final String name;

    StopResult(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public static StopResult from(int flag) {
        for (StopResult value : values()) {
            if (value.flag == flag) {
                return value;
            }
        }
        return null;
    }

    public int getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

}
