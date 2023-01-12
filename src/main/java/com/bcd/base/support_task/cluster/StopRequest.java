package com.bcd.base.support_task.cluster;

public class StopRequest {
    public final String requestId;
    public final String[] ids;

    public StopRequest(String requestId, String[] ids) {
        this.requestId = requestId;
        this.ids = ids;
    }
}
