package com.bcd.base.support_spring_exception.handler.impl;

import cn.dev33.satoken.exception.NotLoginException;
import com.bcd.base.message.ErrorMessage;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class DefaultExceptionResponseHandler implements ExceptionResponseHandler {
    private final HttpMessageConverter converter;

    public DefaultExceptionResponseHandler(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.converter = mappingJackson2HttpMessageConverter;
    }

    public static JsonMessage checkNotLoginException(Throwable throwable) {
        if (throwable instanceof NotLoginException && ((NotLoginException) throwable).getType().equals(NotLoginException.NOT_TOKEN)) {
            return JsonMessage.fail().message(NotLoginException.DEFAULT_MESSAGE).code("401");
        } else {
            return null;
        }
    }

    @Override
    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        Throwable realException = ExceptionUtil.parseRealException(throwable);
        JsonMessage result = Optional.ofNullable(checkNotLoginException(realException)).orElse(ExceptionUtil.toJsonMessage(realException));
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
