package com.base.i18n;

import com.base.util.I18nUtil;
import org.springframework.util.StringUtils;

/**
 * Created by Administrator on 2017/7/26.
 */
public class I18NData {
    private String key;
    private String value;

    public I18NData(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        if(StringUtils.isEmpty(value)){
            value= I18nUtil.getMessage(key);
        }
        return value;
    }
}
