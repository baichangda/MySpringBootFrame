package com.config.exception;

import com.alibaba.fastjson.JSONObject;
import com.base.exception.BaseRuntimeException;
import com.base.json.JsonMessage;
import com.base.message.ErrorMessage;
import com.base.util.I18nUtil;
import com.config.shiro.ShiroConst;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Configuration
public class CustomExceptionHandler extends DefaultHandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object object, Exception exception) {
        //1、先打印出异常信息
        exception.printStackTrace();
        //2、判断response
        if(!response.isCommitted()){
            try {
                JsonMessage result;
                //2.1、先验证是否属于自定义运行异常
                if(BaseRuntimeException.class.isAssignableFrom(exception.getClass())){
                    result=((BaseRuntimeException)exception).toJsonMessage();
                }else{
                    //2.2、非自定义异常处理
                    //2.2.1、先验证是否属于shiro的异常类型
                    ErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(exception.getClass().getName());
                    if(errorMessage!=null){
                        result=errorMessage.toJsonMessage();
                    }else{
                        //2.2.2、否则当作普通异常处理,直接返回
                        result=JsonMessage.failed(exception.getMessage());
                    }
                }

                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(JSONObject.toJSONString(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModelAndView();
    }

}