package com.bcd.sys.task;

public class StopSysTask {
    private String code;
    private Boolean mayInterruptIfRunning;
    private String[] ids;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }

    public void setMayInterruptIfRunning(Boolean mayInterruptIfRunning) {
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }
}
