package com.bcd.base.support_spring_exception.handler.impl;

import com.bcd.base.support_shiro.ShiroConst;
import com.bcd.base.message.ErrorMessage;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import com.bcd.base.util.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class DefaultExceptionResponseHandler implements ExceptionResponseHandler {
    private final HttpMessageConverter converter;

    public DefaultExceptionResponseHandler(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.converter = mappingJackson2HttpMessageConverter;
    }

    @Override
    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        Throwable realException = ExceptionUtil.parseRealException(throwable);
        ErrorMessage errorMessage = ShiroConst.EXCEPTION_ERROR_MESSAGE_MAP.get(realException.getClass());
        JsonMessage result;
        if (errorMessage == null) {
            result = ExceptionUtil.toJsonMessage(realException);
        } else {
            result = errorMessage.toJsonMessage();
        }
        handle(response, result);
    }


    @Override
    public void handle(HttpServletResponse response, Object result) throws IOException {
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(response);
        converter.write(result,
                MediaType.APPLICATION_JSON,
                servletServerHttpResponse);
    }
}
