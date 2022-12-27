package com.bcd.base.support_jdbc.code;


import com.bcd.base.support_jdbc.dbinfo.data.DBInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    //生成文件的目标文件夹路径
    public String targetDirPath;
    //模版文件夹路径
    public String templateDirPath;
    //表配置
    public List<TableConfig> tableConfigs = new ArrayList<>();
    //数据库
    public DBInfo dbInfo;

    private Config(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    public static Config newConfig(String targetDirPath, DBInfo dbInfo) {
        return new Config(targetDirPath);
    }

    public static Config newConfig(String targetDirPath) {
        return new Config(targetDirPath);
    }

    public Config addTableConfig(TableConfig... tableConfigs) {
        this.tableConfigs.addAll(Arrays.asList(tableConfigs));
        for (TableConfig tableConfig : tableConfigs) {
            tableConfig.config=this;
        }
        return this;
    }

    public Config addTableConfig(List<TableConfig> tableConfigs) {
        this.tableConfigs.addAll(tableConfigs);
        for (TableConfig tableConfig : tableConfigs) {
            tableConfig.config=this;
        }
        return this;
    }
}
