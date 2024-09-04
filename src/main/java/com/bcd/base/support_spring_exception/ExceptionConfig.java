package com.bcd.base.support_spring_exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.bcd.base.result.Result;
import com.bcd.base.util.ExceptionUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionConfig {
    private final static Logger logger = LoggerFactory.getLogger(ExceptionConfig.class);

    @ExceptionHandler
    public void handlerException(HttpServletResponse response, Exception exception) {
        //1、判断response
        if (response.isCommitted()) {
            return;
        }
        //2、打印异常
        logger.error("Error", exception);

        //3、使用异常handler处理异常
        try {
            handle(response, exception);
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }

    public enum ExceptionCode {
        not_login(401, "请先登陆"),
        arg_error(501, "参数错误、请联系开发人员");
        final int code;
        final String msg;

        ExceptionCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    public void handle(HttpServletResponse response, Throwable throwable) throws IOException {
        Throwable realException = ExceptionUtil.parseException(throwable);
        Result<?> result;
        if (realException instanceof MethodArgumentNotValidException) {
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
            result = Result.fail(ExceptionCode.arg_error.code, errorList).message(ExceptionCode.arg_error.msg);
        } else {
            result = Result.from(realException);
        }
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(result.toJson());
        response.getWriter().flush();
    }
}
