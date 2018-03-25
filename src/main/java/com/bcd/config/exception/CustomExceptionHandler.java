package com.bcd.config.exception;

import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CustomExceptionHandler extends DefaultHandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object object, Exception exception) {
        //1、判断response
        if(response.isCommitted()){
            return new ModelAndView();
        }
        //2、获取异常JsonMessage
        JsonMessage result= ExceptionUtil.toJsonMessage(exception);
        //3、打印异常
        ExceptionUtil.printException(exception);
        try {
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(JsonUtil.toJSONResult(result));
        }catch (IOException e){
            e.printStackTrace();
        }
        return new ModelAndView();
    }

}