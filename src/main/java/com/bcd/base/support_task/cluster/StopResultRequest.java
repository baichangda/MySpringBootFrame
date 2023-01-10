package com.bcd.base.support_task.cluster;

import java.util.HashMap;

public class StopResultRequest {
    public final String requestId;
    public final HashMap<String,String> resMap=new HashMap<>();

    public StopResultRequest(String requestId) {
        this.requestId = requestId;
    }
}
