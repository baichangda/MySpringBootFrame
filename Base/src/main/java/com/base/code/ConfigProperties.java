package com.base.code;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/28.
 */
public class ConfigProperties {
    private String dirPath;
    private boolean needCreateInfo=true;
    private Map<String,String> valueMap=new HashMap<>();
    private Map<String,Object> dataMap=new HashMap<>();

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public boolean isNeedCreateInfo() {
        return needCreateInfo;
    }

    public void setNeedCreateInfo(boolean needCreateInfo) {
        this.needCreateInfo = needCreateInfo;
    }

    public Map<String, String> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
}
