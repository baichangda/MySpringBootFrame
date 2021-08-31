package com.bcd.base.support_jpa.code;

import com.bcd.base.support_jpa.dbinfo.data.DBInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class Config {
    //生成文件的目标文件夹路径
    private String targetDirPath;
    //模版文件夹路径
    private String templateDirPath;
    //表配置
    private List<TableConfig> tableConfigs = new ArrayList<>();
    //数据库
    private DBInfo dbInfo;

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
            tableConfig.setConfig(this);
        }
        return this;
    }

    public Config addTableConfig(List<TableConfig> tableConfigs) {
        this.tableConfigs.addAll(tableConfigs);
        for (TableConfig tableConfig : tableConfigs) {
            tableConfig.setConfig(this);
        }
        return this;
    }
}
