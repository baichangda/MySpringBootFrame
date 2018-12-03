package com.bcd.config.shiro;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

public class MyCookieRememberMeManager extends CookieRememberMeManager{
    public static final String DEFAULT_AES_BASE64_KEY ="s2SE9y32PvLeYo+VGFpcKA==";

    public MyCookieRememberMeManager() {
        //为了避免集群时候多个服务器采用不一样的key,在此生成固定的key
        setCipherKey(Base64.decode(DEFAULT_AES_BASE64_KEY));
    }
}
