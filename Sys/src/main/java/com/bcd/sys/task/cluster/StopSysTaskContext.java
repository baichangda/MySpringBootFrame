package com.bcd.sys.task.cluster;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 停止任务 上下文
 */
public class StopSysTaskContext {
    private String code;
    private String[] ids;
    private Map<String,Boolean> result=new ConcurrentHashMap<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public Map<String, Boolean> getResult() {
        return result;
    }

    public void setResult(Map<String, Boolean> result) {
        this.result = result;
    }

}
