package com.bcd.base.support_task.cluster;

import java.util.HashMap;

public class StopResultRequest {
    private final String requestId;
    private final HashMap<String,String> resMap=new HashMap<>();

    public StopResultRequest(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public HashMap<String, String> getResMap() {
        return resMap;
    }
}
