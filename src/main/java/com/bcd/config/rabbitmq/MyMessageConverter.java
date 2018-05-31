package com.bcd.config.rabbitmq;

import com.bcd.base.exception.BaseRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/6/22.
 */
@SuppressWarnings("unchecked")
public class MyMessageConverter implements MessageConverter{
    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        return null;
    }

    /**
     * 目前只支持
     * 1、String
     * 2、Map 返回类型为 JSONObject
     * 3、List 返回类型为 JSONArray
     * 4、JavaBean
     * @param message
     * @return
     * @throws MessageConversionException
     */
    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        MessageProperties properties = message.getMessageProperties();
        if(properties==null){
            return message;
        }
        String encoding=properties.getContentEncoding();
        if(encoding==null){
            encoding="UTF-8";
        }
        String content;
        try {
            content=new String(message.getBody(),encoding);
        } catch (UnsupportedEncodingException e) {
            throw BaseRuntimeException.getException(e);
        }
        Class clazz= (Class)properties.getInferredArgumentType();
        try {
            if(clazz.isAssignableFrom(String.class)){
                return content;
            }else{
                return new ObjectMapper().readValue(content,clazz);
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
