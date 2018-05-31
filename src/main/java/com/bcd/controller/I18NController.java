package com.bcd.controller;

import com.bcd.base.message.JsonMessage;
import com.bcd.define.ErrorDefine;
import com.bcd.define.SuccessDefine;
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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lang",value = "语言标识",dataType = "String",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "是否切换成功")})
    public JsonMessage<Object> changeLocal(HttpServletRequest request,
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
                    return ErrorDefine.ERROR_CHANGE_LOCALE.toJsonMessage(lang);
                }
            }
        }
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale);
        return SuccessDefine.SUCCESS_CHANGE_LOCALE.toJsonMessage();
    }
}
