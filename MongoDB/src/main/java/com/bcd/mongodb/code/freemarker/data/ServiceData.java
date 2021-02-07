package com.bcd.mongodb.code.freemarker.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class ServiceData {
    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 模块中文名
     */
    private String moduleNameCN;

    /**
     * 包路径
     */
    private String packagePre;

    /**
     * 主键类型
     */
    private String pkType;

}
