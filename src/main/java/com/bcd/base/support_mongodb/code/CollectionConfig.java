package com.bcd.base.support_mongodb.code;


import com.bcd.base.exception.MyException;
import com.bcd.base.support_mongodb.code.data.CodeConst;

import java.nio.file.Paths;

public class CollectionConfig {
    //模版文件夹路径
    public String templateDirPath;
    //生成文件的目标文件夹路径
    public String targetDirPath;
    //模块名(英文)
    public String moduleName;
    //模块名(中文)
    public String moduleNameCN;
    //类名
    public Class<?> clazz;
    //是否创建repository文件(默认是)
    public boolean needCreateRepositoryFile = true;
    //是否创建service文件(默认是)
    public boolean needCreateServiceFile = true;
    //是否创建controller文件(默认是)
    public boolean needCreateControllerFile = true;
    //是否需要加上controller save方法的验证注解
    public boolean needValidateSaveParam = false;

    public CollectionConfig(String moduleName, String moduleNameCN, Class<?> clazz) {
        this.moduleName = moduleName;
        this.moduleNameCN = moduleNameCN;
        this.clazz = clazz;
        parseTargetDirPath();
    }

    private void parseTargetDirPath() {
        //根据class路径找到源文件路径
        String classFilePath = clazz.getResource("").getFile();
        if (System.getProperty("os.name").startsWith("Windows")) {
            classFilePath = classFilePath.substring(1);
        }
        String beanPath;
        if (classFilePath.contains(CodeConst.CLASS_OUT_DIR_PATH)) {
            //替换out目录下
            beanPath = classFilePath.replace(CodeConst.CLASS_OUT_DIR_PATH, CodeConst.SOURCE_DIR_PATH);
        } else if (classFilePath.contains(CodeConst.CLASS_BUILD_DIR_PATH)) {
            //替换build目录下
            beanPath = classFilePath.replace(CodeConst.CLASS_BUILD_DIR_PATH, CodeConst.SOURCE_DIR_PATH);
        } else {
            throw MyException.get("parseTargetDirPath failed,class path[" + classFilePath + "] not support");
        }
        targetDirPath = Paths.get(beanPath).getParent().toString();
    }

    public CollectionConfig setClazz(Class<?> clazz) {
        this.clazz = clazz;
        parseTargetDirPath();
        return this;
    }
}