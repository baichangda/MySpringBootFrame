package com.bcd.config.exception;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.message.BaseErrorMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.config.shiro.ShiroConst;
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
                    BaseErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(exception.getClass().getName());
                    if(errorMessage!=null){
                        result=errorMessage.toJsonMessage();
                    }else{
                        //2.2.2、否则当作普通异常处理,直接返回
                        result=JsonMessage.failed(exception.getMessage(),null,exception.toString());
                    }
                }

                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(JsonUtil.toDefaultJSONString(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModelAndView();
    }

}