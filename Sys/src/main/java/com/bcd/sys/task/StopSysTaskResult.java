package com.bcd.sys.task;

import java.util.HashMap;
import java.util.Map;

public class StopSysTaskResult {
    private String code;
    private Map<String,Boolean> result=new HashMap<>();

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
