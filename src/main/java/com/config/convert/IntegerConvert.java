package com.config.convert;


import org.springframework.core.convert.converter.Converter;

/**
 * Created by Administrator on 2017/7/20.
 */
public class IntegerConvert implements Converter<String,Integer> {
    @Override
    public Integer convert(String source) {
        return Integer.parseInt(source);
    }
}
