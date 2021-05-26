package com.bcd.base.support_mongodb.code.freemarker.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class ControllerData {
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
     * controller映射路径
     */
    private String requestMappingPre;

    /**
     * 字段集合
     */
    private List<BeanField> fieldList;

    /**
     * 是否需要保存方法验证
     */
    private boolean validateSaveParam = true;

    /**
     * 主键类型
     */
    private String pkType;

}
