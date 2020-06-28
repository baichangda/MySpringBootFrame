package com.bcd.config.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Created by Administrator on 2017/7/12.
 *
 */
@Configuration
public class I18NConfig {
    /**
     * 开启i18n header区域解析器
     * 通过设置 http header Accept-Language
     * zh:中文
     * en:英文
     * 来切换
     * @return
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver slr = new AcceptHeaderLocaleResolver();
        slr.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return slr;
    }
}
