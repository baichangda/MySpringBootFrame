package com.bcd.base.util;

import org.springframework.core.io.ClassPathResource;


/**
 * Created by Administrator on 2017/5/25.
 */
public class SpringUtil{

    public static ClassPathResource getResource(String path) {
        return new ClassPathResource(path);
    }
}
