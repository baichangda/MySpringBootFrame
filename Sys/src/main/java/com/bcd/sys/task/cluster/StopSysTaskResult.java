package com.bcd.sys.task;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StopSysTaskResult {
    private String code;
    private Map<String,Boolean> result=new ConcurrentHashMap<>();
    private Set<String> localIds=new HashSet<>();

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

    public Set<String> getLocalIds() {
        return localIds;
    }

    public void setLocalIds(Set<String> localIds) {
        this.localIds = localIds;
    }
}
