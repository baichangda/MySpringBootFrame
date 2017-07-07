package com.base.util;

/**
 * Created by Administrator on 2017/6/9.
 */
public class SystemUtil {
    public enum OSType{
        WINDOW,
        LINUX,
        MAC,
        UNKNOWN
    }

    /**
     * 取出当前jvm使用的操作系统、仅识别 OSType 的操作系统
     * @return
     */
    public static OSType getSystemOS(){
       String osName= System.getProperty("os.name");
       String upperCaseOsName= osName.toUpperCase();
        if(upperCaseOsName.startsWith("WIN")){
            return OSType.WINDOW;
        }else if(upperCaseOsName.startsWith("LINUX")){
            return OSType.LINUX;
        }else if(upperCaseOsName.startsWith("MAC")){
            return OSType.MAC;
        }else {
            return OSType.UNKNOWN;
        }
    }
}
