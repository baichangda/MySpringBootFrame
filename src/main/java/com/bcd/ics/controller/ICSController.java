package com.bcd.ics.controller;

import com.incarcloud.skeleton.Starter;
import com.incarcloud.skeleton.config.Config;
import com.incarcloud.skeleton.context.Context;
import com.incarcloud.skeleton.exception.BaseRuntimeException;
import com.incarcloud.skeleton.exception.NoHandlerException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/ics")
public class ICSController {
    Context context;

    public ICSController(){
        this.context=Starter.getContext();
        Config config= this.context.getConfig();
        //配置访问前缀 为 /ics + /test
        config.withRequestMappingPre("/ics/test");
        //添加ics组件扫描包路径,jar默认包路径为 com.incarcloud;例如我这里包名为com.bcd
        config.addScanPackages("com.bcd");
        //根据配置初始化
        this.context.init();
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/test/**",method = RequestMethod.GET)
    @ApiOperation(value = "测试ics",notes = "测试ics")
    @ApiResponse(code = 200,message = "测试ics")
    public void test(HttpServletRequest request, HttpServletResponse response){
        try {
            context.handle(request,response);
        } catch (NoHandlerException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
