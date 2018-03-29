package com.bcd.config.exception;

import com.bcd.base.json.JsonMessage;
import com.bcd.base.message.BaseErrorMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.config.define.ErrorDefine;
import com.bcd.config.shiro.ShiroConst;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@SuppressWarnings("unchecked")
public class CustomExceptionHandler extends DefaultHandlerExceptionResolver {
    @Autowired
    @Qualifier("fastJsonHttpMessageConverter")
    private HttpMessageConverter converter;
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object object, Exception exception) {
        //1、判断response
        if(response.isCommitted()){
            return new ModelAndView();
        }
        //2、获取异常JsonMessage,此处可以做异常翻译
        JsonMessage result;
        BaseErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(exception.getClass().getName());
        if(errorMessage==null){
            result=ExceptionUtil.toJsonMessage(exception);
        }else{
            result=errorMessage.toJsonMessage();
        }
        //3、打印异常
        ExceptionUtil.printException(exception);

        //4、返回结果集
        try {
            response(response,result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView();
    }

    /**
     * 采用 spring 自带的转换器转换结果,输出结果
     * @param response
     * @throws IOException
     */
    private void response(HttpServletResponse response,JsonMessage res) throws IOException {
        ServletServerHttpResponse servletServerHttpResponse=new ServletServerHttpResponse(response);
        converter.write(res, MediaType.APPLICATION_JSON_UTF8, servletServerHttpResponse);
    }

}