package com.bcd.sys.task.cluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StopSysTaskResult {
    private String code;
    private Map<String,Boolean> result=new ConcurrentHashMap<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Boolean> getResult() {
        return result;
    }

    public void setResult(Map<String, Boolean> result) {
        this.result = result;
    }

}
