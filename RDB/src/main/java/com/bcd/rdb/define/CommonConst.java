package com.bcd.rdb.define;

public class CommonConst {
    public final static int LOGIC_DELETE_VAL=1;
    public final static int LOGIC_NOT_DELETE_VAL=0;
    public final static String LOGIC_DELETE_COLUMN="is_del";
    public final static String LOGIC_NOT_DELETE_WHERE_SQL=LOGIC_DELETE_COLUMN+"="+LOGIC_NOT_DELETE_VAL;
    public final static String LOGIC_DELETE_UPDATE_SQL=LOGIC_DELETE_COLUMN+"="+LOGIC_DELETE_VAL;
}
