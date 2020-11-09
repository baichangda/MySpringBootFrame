package com.bcd.mongodb.code.freemarker.data;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private boolean validateSaveParam=true;

    /**
     * 主键类型
     */
    private String pkType;

}
