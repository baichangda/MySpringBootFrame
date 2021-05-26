package com.bcd.base.support_rdb.code;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.StringUtil;
import com.bcd.base.support_rdb.code.data.BeanField;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CodeGeneratorContext {
    private TableConfig tableConfig;
    private DBSupport dbSupport;
    private Connection connection;


    //以下是cache字段
    private String pkType;
    private List<BeanField> allBeanFields;
    private List<BeanField> declaredBeanFields;
    private String packagePre;


    public CodeGeneratorContext(TableConfig tableConfig, DBSupport dbSupport, Connection connection) {
        this.tableConfig = tableConfig;
        this.dbSupport = dbSupport;
        this.connection = connection;
    }

    /**
     * 初始化主键类型
     */
    public String getPkType() {
        if (pkType == null) {
            pkType = dbSupport.getTablePkType(tableConfig, connection).toString();
        }
        return pkType;
    }

    /**
     * 获取实体类定义字段信息(排除公用信息 id/create/update信息)
     */
    public List<BeanField> getDeclaredBeanFields() {
        if (declaredBeanFields == null) {
            declaredBeanFields = getAllBeanFields().stream().filter(e -> {
                if ("id".equals(e.getName())) {
                    return false;
                } else {
                    if (tableConfig.isNeedCreateInfo()) {
                        if (CodeConst.CREATE_INFO_FIELD_NAME.contains(e.getName())) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }).collect(Collectors.toList());
        }
        return declaredBeanFields;
    }

    /**
     * 获取所有字段信息
     *
     * @return
     */
    public List<BeanField> getAllBeanFields() {
        if (allBeanFields == null) {
            allBeanFields = dbSupport.getTableBeanFieldList(tableConfig, connection);
        }
        return allBeanFields;
    }


    /**
     * 初始化包名
     * 初始化当前表生成代码目录父包名
     */
    public String getPackagePre() {
        if (packagePre == null) {
            StringBuilder springSrcPathSb = new StringBuilder();
            springSrcPathSb.append("src");
            springSrcPathSb.append(File.separatorChar);
            springSrcPathSb.append("main");
            springSrcPathSb.append(File.separatorChar);
            springSrcPathSb.append("java");
            springSrcPathSb.append(File.separatorChar);
            String springSrcPath = springSrcPathSb.toString();
            String targetDirPath = tableConfig.getConfig().getTargetDirPath();
            if (targetDirPath.contains(springSrcPath)) {
                packagePre = targetDirPath.split(StringUtil.escapeExprSpecialWord(springSrcPath))[1].replaceAll(StringUtil.escapeExprSpecialWord(File.separator), ".");
            } else {
                throw BaseRuntimeException.getException("targetDirPath[" + targetDirPath + "] must contains [" + springSrcPath + "]");
            }
        }
        return packagePre;
    }

    /**
     * 初始化request mapping
     *
     * @return
     */
    public String getRequestMappingPre() {
        return "/" + getPackagePre().substring(getPackagePre().lastIndexOf('.') + 1);
    }
}
