package com.bcd.base.support_task.cluster;

public class StopRequest {
    private String requestId;
    private String[] ids;

    public StopRequest(String requestId, String[] ids) {
        this.requestId = requestId;
        this.ids = ids;
    }

    public String getRequestId() {
        return requestId;
    }

    public String[] getIds() {
        return ids;
    }
}
