package com.bcd.base.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Created by Administrator on 2017/3/15.
 */
@Component
public class I18nUtil {

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        I18nUtil.messageSource = messageSource;
    }

    /**
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        return getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * @param key
     * @param locale
     * @return
     */
    public static String getMessage(String key, Locale locale) {
        return getMessage(key, null, locale);
    }

    /**
     * @param key
     * @param params
     * @param locale
     * @return
     */
    public static String getMessage(String key, Object[] params, Locale locale) {
        return messageSource.getMessage(key, params, locale);
    }

    /**
     * @param key
     * @param params
     * @return
     */
    public static String getMessage(String key, Object[] params) {
        return getMessage(key, params, LocaleContextHolder.getLocale());
    }
}
