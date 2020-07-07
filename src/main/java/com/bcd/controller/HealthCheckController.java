package com.bcd.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthCheckController {
    @RequestMapping(value = "/",method = RequestMethod.GET)
    @ApiOperation(value = "健康检查(默认访问路径)",notes = "健康检查(默认访问路径)")
    @ApiResponse(code = 200,message = "健康检查(默认访问路径)")
    public void healthCheck(){
    }
}
