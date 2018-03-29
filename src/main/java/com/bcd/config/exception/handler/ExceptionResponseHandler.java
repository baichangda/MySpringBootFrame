package com.bcd.config.exception.handler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExceptionResponseHandler {
    void handle(HttpServletResponse response, Throwable throwable) throws IOException;
}
