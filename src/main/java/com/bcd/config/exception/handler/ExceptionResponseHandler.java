package com.bcd.config.exception.handler;

import com.bcd.base.message.ErrorMessage;
import com.bcd.base.message.Message;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExceptionResponseHandler {
    /**
     * 解析异常并返回到客户端
     * @param response
     * @param throwable 异常
     * @throws IOException
     */
    void handle(HttpServletResponse response, Throwable throwable) throws IOException;

    /**
     * 返回信息到客户端
     * @param response
     * @param result 数据
     * @throws IOException
     */
    void handle(HttpServletResponse response, Object result) throws IOException;
}
