package com.bcd.rdb.code;


import java.util.HashMap;
import java.util.Map;

public class Config {
    //生成文件的目标文件夹路径
    public String targetDirPath;
    //模版文件夹路径
    public String templateDirPath;
    //表配置
    public TableConfig[] tableConfigs;
    //存储运行中的数据
    public Map<String,Object> dataMap=new HashMap<>();
    //存储即将替换模版值数据
    public Map<String,Object> valueMap=new HashMap<>();

    public Config(String targetDirPath, TableConfig... tableConfigs) {
        this.targetDirPath = targetDirPath;
        this.tableConfigs = tableConfigs;
        if(tableConfigs!=null){
            for (TableConfig tableConfig : tableConfigs) {
                tableConfig.setConfig(this);
            }
        }
    }

    public String getTargetDirPath() {
        return targetDirPath;
    }

    public void setTargetDirPath(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    public String getTemplateDirPath() {
        return templateDirPath;
    }

    public void setTemplateDirPath(String templateDirPath) {
        this.templateDirPath = templateDirPath;
    }

    public TableConfig[] getTableConfigs() {
        return tableConfigs;
    }

    public void setTableConfigs(TableConfig[] tableConfigs) {
        this.tableConfigs = tableConfigs;
    }


}
