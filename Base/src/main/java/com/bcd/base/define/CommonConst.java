package com.bcd.base.define;

import java.util.Date;

public class CommonConst {
    /**
     * 参数验证失败时候的错误编码
     * 对应的是
     * {@link javax.validation.constraints} 中的验证注解错误
     */
    public static String PARAM_VALIDATE_FAILED_CODE="99999";

    /**
     * 八大基础数据类型的原始类和封装类 和 Date
     */
    public final static Class[] BASE_DATA_TYPE = new Class[]{
            Integer.class, String.class, Double.class, Character.class, Byte.class, Float.class,
            Long.class, Short.class, Boolean.class, Date.class,
            int.class, byte.class, short.class, char.class, double.class, float.class, long.class, boolean.class};
}
