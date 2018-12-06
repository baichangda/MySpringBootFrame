package com.bcd.mongodb.code;


import java.util.HashMap;
import java.util.Map;

public class Config {
    //模版文件夹路径
    public String templateDirPath;
    //表配置
    public CollectionConfig[] collectionConfigs;
    //存储运行中的数据
    public Map<String,Object> dataMap=new HashMap<>();
    //存储即将替换模版值数据
    public Map<String,Object> valueMap=new HashMap<>();

    public Config(CollectionConfig... collectionConfigs) {
        this.collectionConfigs = collectionConfigs;
        if(collectionConfigs !=null){
            for (CollectionConfig collectionConfig : collectionConfigs) {
                collectionConfig.setConfig(this);
            }
        }
    }

    public String getTemplateDirPath() {
        return templateDirPath;
    }

    public void setTemplateDirPath(String templateDirPath) {
        this.templateDirPath = templateDirPath;
    }

    public CollectionConfig[] getCollectionConfigs() {
        return collectionConfigs;
    }

    public void setCollectionConfigs(CollectionConfig[] collectionConfigs) {
        this.collectionConfigs = collectionConfigs;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, Object> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, Object> valueMap) {
        this.valueMap = valueMap;
    }
}
