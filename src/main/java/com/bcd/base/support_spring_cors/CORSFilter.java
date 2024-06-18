package com.bcd.base.support_spring_cors;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;
        // 手动设置响应头解决跨域访问
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        // 支持 HTTP 1.1
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        // 支持 HTTP 1.0.
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "no-cache");
        // 编码
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, resp);
    }
}