package com.bcd.rdb.code;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Config {
    //生成文件的目标文件夹路径
    private String targetDirPath;
    //模版文件夹路径
    private String templateDirPath;
    //表配置
    private List<TableConfig> tableConfigs=new ArrayList<>();
    //数据库
    private String db;

    private Config(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    public static Config newConfig(String targetDirPath){
        return new Config(targetDirPath);
    }

    public Config addTableConfig(TableConfig ... tableConfigs){
        this.tableConfigs.addAll(Arrays.asList(tableConfigs));
        for (TableConfig tableConfig : tableConfigs) {
            tableConfig.setConfig(this);
        }
        return this;
    }

    public Config addTableConfig(List<TableConfig> tableConfigs){
        this.tableConfigs.addAll(tableConfigs);
        for (TableConfig tableConfig : tableConfigs) {
            tableConfig.setConfig(this);
        }
        return this;
    }
}
