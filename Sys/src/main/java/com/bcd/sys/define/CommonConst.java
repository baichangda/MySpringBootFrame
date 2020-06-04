package com.bcd.sys.define;


import com.bcd.base.config.redis.RedisUtil;

import java.io.Serializable;

public class CommonConst {
    public final static long ADMIN_ID=1L;
    public final static String ADMIN_USERNAME="admin";
    public final static boolean IS_PASSWORD_ENCODED=false;
    public final static String INITIAL_PASSWORD ="123qwe";

    public final static String KICK_SESSION_ID_PRE= RedisUtil.SYSTEM_REDIS_KEY_PRE+"kickSessionId:";

    public final static int KICK_SESSION_EXPIRE_IN_SECOND=60*60;
}
