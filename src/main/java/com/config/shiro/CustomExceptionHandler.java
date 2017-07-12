package com.config.shiro;

import com.alibaba.fastjson.JSONObject;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Configuration
public class CustomExceptionHandler extends DefaultHandlerExceptionResolver {
    public static HashMap<String,String> EXCEPTION_MESSAGE_KEY_MAP=new HashMap<>();
    public static HashMap<String,String> EXCEPTION_CODE_MAP=new HashMap<>();
    static {
        //初始化i18n配置key
        EXCEPTION_MESSAGE_KEY_MAP.put(UnknownAccountException.class.getName(), "CustomExceptionHandler.UnknownAccountException");
        EXCEPTION_MESSAGE_KEY_MAP.put(IncorrectCredentialsException.class.getName(),"CustomExceptionHandler.IncorrectCredentialsException");
        EXCEPTION_MESSAGE_KEY_MAP.put(DisabledAccountException.class.getName(),"CustomExceptionHandler.DisabledAccountException");
        EXCEPTION_MESSAGE_KEY_MAP.put(AuthenticationException.class.getName(),"CustomExceptionHandler.AuthenticationException");
        EXCEPTION_MESSAGE_KEY_MAP.put(UnauthenticatedException.class.getName(),"CustomExceptionHandler.UnauthenticatedException");
        EXCEPTION_MESSAGE_KEY_MAP.put(ExpiredCredentialsException.class.getName(),"CustomExceptionHandler.ExpiredCredentialsException");
        EXCEPTION_MESSAGE_KEY_MAP.put(AuthorizationException.class.getName(),"CustomExceptionHandler.AuthorizationException");


        //初始化exception对应reponse code
        EXCEPTION_CODE_MAP.put(UnknownAccountException.class.getName(),String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
    }


    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object object, Exception exception) {
        //1、先打印出异常信息
        exception.printStackTrace();
        //2、判断response
        if(!response.isCommitted()){
            try {
                String msg;
                String error=exception.getMessage();
                String key= EXCEPTION_MESSAGE_KEY_MAP.get(exception.getClass().getName());
                String code= EXCEPTION_CODE_MAP.get(exception.getClass().getName());
                if(key==null){
                    msg=error;
                }else{
                    msg=I18nUtil.getMessage(key);
                }
                JsonMessage<Object> result=new JsonMessage<>(false,msg,error,code);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(JSONObject.toJSONString(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModelAndView();
    }

}