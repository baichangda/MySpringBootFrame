package com.bcd.controller;

import com.bcd.base.message.JsonMessage;
import com.bcd.define.MessageDefine;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by Administrator on 2017/5/17.
 */
@RestController
@RequestMapping("/api/i18n")
public class I18NController {

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/changeLocal",method = RequestMethod.POST)
    @ApiOperation(value = "切换语言",notes = "切换语言")
    @ApiResponse(code = 200,message = "切换结果")
    public JsonMessage<Object> changeLocal(
            HttpServletRequest request,
            @ApiParam(value = "语言标识(zh:中文;en:英文)")
            @RequestParam(value="lang",required = false) String lang){
        Locale locale=Locale.getDefault();
        if(lang!=null){
            switch(lang){
                case "zh":{
                    locale=new Locale("zh","CN");
                    break;
                }
                case "en":{
                    locale=new Locale("en","US");
                    break;
                }
                default:{
                    return MessageDefine.ERROR_CHANGE_LOCALE.toJsonMessage(lang);
                }
            }
        }
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale);
        return MessageDefine.SUCCESS_CHANGE_LOCALE.toJsonMessage(true);
    }
}
