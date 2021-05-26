package com.bcd.base.support_spring_exception.handler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExceptionResponseHandler {
    /**
     * 解析异常并返回到客户端
     *
     * @param response
     * @param throwable 异常
     * @throws IOException
     */
    void handle(HttpServletResponse response, Throwable throwable) throws IOException;

    /**
     * 返回信息到客户端
     *
     * @param response
     * @param result   数据
     * @throws IOException
     */
    void handle(HttpServletResponse response, Object result) throws IOException;
}
