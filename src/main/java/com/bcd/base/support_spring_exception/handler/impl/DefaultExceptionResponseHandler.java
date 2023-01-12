package com.bcd.base.support_spring_exception.handler.impl;

import cn.dev33.satoken.exception.NotLoginException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.support_spring_exception.handler.ExceptionResponseHandler;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class DefaultExceptionResponseHandler implements ExceptionResponseHandler {

    public enum ExceptionCode {
        not_login(401,"请先登陆"),
        arg_error(501,"参数错误、请联系开发人员");
        final int code;
        final String msg;

        ExceptionCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    private final HttpMessageConverter converter;

    public DefaultExceptionResponseHandler(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.converter = mappingJackson2HttpMessageConverter;
    }

    @Override
    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        Throwable realException = ExceptionUtil.parseRealException(throwable);
        JsonMessage result;
        if (realException instanceof NotLoginException) {
            result = JsonMessage.fail(ExceptionCode.not_login.code).message(ExceptionCode.not_login.msg);
        } else if (realException instanceof MethodArgumentNotValidException) {
            final BindingResult bindingResult = ((MethodArgumentNotValidException) realException).getBindingResult();
            final List<ObjectError> allErrors = bindingResult.getAllErrors();
            final List<Map<String, String>> errorList = allErrors.stream().map(e -> {
                Map<String, String> msgMap = new HashMap<>();
                final String defaultMessage = e.getDefaultMessage();
                final String field = ((FieldError) e).getField();
                msgMap.put("field", field);
                msgMap.put("msg", defaultMessage);
                return msgMap;
            }).collect(Collectors.toList());
            result = JsonMessage.fail(ExceptionCode.arg_error.code, errorList).message(ExceptionCode.arg_error.msg);
        } else {
            result = ExceptionUtil.toJsonMessage(realException);
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
