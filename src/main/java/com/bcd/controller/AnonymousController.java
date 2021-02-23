package com.bcd.controller;

import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.keys.KeysConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
//@Tag(name = "AnonymousController",description = "公开")
public class AnonymousController extends BaseController {
    Logger logger = LoggerFactory.getLogger(AnonymousController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPublicKey", method = RequestMethod.GET)
    @Operation(description = "获取公钥")
    @ApiResponse(responseCode = "200", description = "公钥信息")
    public JsonMessage<String> getPublicKey() {
        return JsonMessage.success().withData(KeysConst.PUBLIC_KEY_BASE64);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getCookie", method = RequestMethod.GET)
    @Operation(description = "获取cookie")
    @ApiResponse(responseCode = "200", description = "当前浏览器的cookie")
    public JsonMessage<String> getCookie() {
        Subject subject = SecurityUtils.getSubject();
        String cookie = Optional.ofNullable(subject).map(Subject::getSession).map(Session::getId).orElse("").toString();
        return JsonMessage.success().withData(cookie);
    }

}
