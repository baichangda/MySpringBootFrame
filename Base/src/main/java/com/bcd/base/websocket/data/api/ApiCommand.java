package com.bcd.base.websocket.data.api;

public class ApiCommand {
    private String apiName;
    private String paramJson;

    public ApiCommand(String apiName, String paramJson) {
        this.apiName = apiName;
        this.paramJson = paramJson;
    }

    public ApiCommand() {
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }
}
