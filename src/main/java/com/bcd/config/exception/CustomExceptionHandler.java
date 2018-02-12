package com.bcd.config.exception;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.message.BaseErrorMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.config.shiro.ShiroConst;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
        JsonMessage result= toJsonMessage(exception);
        //3、打印异常
        printException(exception);
        try {
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(JsonUtil.toJSONResult(result));
        }catch (IOException e){
            e.printStackTrace();
        }
        return new ModelAndView();
    }

    /**
     * 根据异常生成对应的JsonMessage
     */
    private JsonMessage toJsonMessage(Exception exception){
        JsonMessage result;
        //1.1、先验证是否属于自定义运行异常
        if(BaseRuntimeException.class.isAssignableFrom(exception.getClass())){
            result=((BaseRuntimeException)exception).toJsonMessage();
        }else{
            //1.2、非自定义异常处理
            //1.2.1、先验证是否属于shiro的异常类型
            BaseErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(exception.getClass().getName());
            if(errorMessage!=null){
                result=errorMessage.toJsonMessage();
            }else{
                //1.2.2、否则当作普通异常处理,直接返回
                result=JsonMessage.failed(exception.getMessage(),null,exception.toString());
            }
        }
        return result;
    }

    /**
     * 如果异常不是BaseRuntimeException,直接打印
     *
     * 如果异常是BaseRuntimeException,则取其真实异常;若真实异常不存在,直接打印异常
     *
     * 如果真实异常存在,判断真实异常是否属于反射调用方法异常,是则获取真实异常的目标异常打印,否则直接打印真实异常
     * @param exception
     */
    private void printException(Exception exception){
        if(BaseRuntimeException.class.isAssignableFrom(exception.getClass())){
            if(((BaseRuntimeException) exception).realException==null){
                exception.printStackTrace();
            }else{
                //打印异常,如果当前异常有真实异常;判断真实异常为反射执行异常则继续获取真实异常的目标异常打印;否则直接打印真实异常
                if(((BaseRuntimeException) exception).realException instanceof InvocationTargetException) {
                    ((InvocationTargetException) ((BaseRuntimeException) exception).realException).getTargetException().printStackTrace();
                }else{
                    ((BaseRuntimeException) exception).realException.printStackTrace();
                }
            }
        }else{
            exception.printStackTrace();
        }
    }

}