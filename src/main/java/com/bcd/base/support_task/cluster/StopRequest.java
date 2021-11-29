package com.bcd.base.support_task.cluster;

public class StopRequest {
    private final String requestId;
    private final String[] ids;

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
