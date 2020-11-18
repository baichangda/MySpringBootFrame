package com.bcd.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.keys.KeysConst;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


/**
 * Created by Administrator on 2017/6/15.
 */
@RestController
@RequestMapping("/api/anonymous")
@Api(tags = "公开/AnonymousController")
public class AnonymousController extends BaseController{
    Logger logger= LoggerFactory.getLogger(AnonymousController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPublicKey",method = RequestMethod.GET)
    @ApiOperation(value = "获取公钥",notes = "获取公钥")
    @ApiResponse(code = 200,message = "公钥信息",response = JsonMessage.class)
    public JsonMessage<String> getPublicKey(){
        return JsonMessage.success().withData(KeysConst.PUBLIC_KEY_BASE64);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getCookie",method = RequestMethod.GET)
    @ApiOperation(value = "获取cookie",notes = "获取cookie")
    @ApiResponse(code = 200,message = "当前浏览器的cookie")
    public JsonMessage<String> getCookie(){
        Subject subject=SecurityUtils.getSubject();
        String cookie=Optional.ofNullable(subject).map(Subject::getSession).map(Session::getId).orElse("").toString();
        return JsonMessage.success().withData(cookie);
    }

}
