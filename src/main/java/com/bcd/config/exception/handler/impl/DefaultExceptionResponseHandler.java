package com.bcd.config.exception.handler.impl;

import com.bcd.base.json.JsonMessage;
import com.bcd.base.message.BaseErrorMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.config.exception.handler.ExceptionResponseHandler;
import com.bcd.config.shiro.ShiroConst;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultExceptionResponseHandler implements ExceptionResponseHandler{
    private HttpMessageConverter converter;

    public DefaultExceptionResponseHandler(HttpMessageConverter converter) {
        this.converter = converter;
    }

    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        ServletServerHttpResponse servletServerHttpResponse=new ServletServerHttpResponse(httpResponse);
        BaseErrorMessage errorMessage= ShiroConst.EXCEPTION_ERRORMESSAGE_MAP.get(throwable.getClass().getName());
        JsonMessage result;
        if(errorMessage==null){
            result= ExceptionUtil.toJsonMessage(throwable);
        }else{
            result=errorMessage.toJsonMessage();
        }
        converter.write(result,
                MediaType.APPLICATION_JSON_UTF8,
                servletServerHttpResponse);
    }
}
