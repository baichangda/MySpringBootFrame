package com.bcd.base.support_mongodb.code.freemarker.data;

import freemarker.template.Configuration;
import freemarker.template.Version;

import java.util.Date;

/**
 * Created by Administrator on 2017/8/14.
 */
public class CodeConst {
    public final static Version FREEMARKER_VERSION = Configuration.VERSION_2_3_30;

    public final static String CLASS_OUT_DIR_PATH = "out/production/classes";
    public final static String CLASS_BUILD_DIR_PATH = "build/classes/java/main";
    public final static String SOURCE_DIR_PATH = "src/main/java";

    public final static String TEMPLATE_DIR_PATH = System.getProperty("user.dir") + "/MongoDB/src/main/resources/template";

    public final static Class[] SUPPORT_FIELD_TYPE = new Class[]{
            Integer.class, String.class, Double.class, Character.class, Byte.class, Float.class,
            Long.class, Short.class, Boolean.class, Date.class,
            int.class, byte.class, short.class, char.class, double.class, float.class, long.class, boolean.class};
}
