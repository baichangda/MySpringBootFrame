package com.bcd.controller;

import com.bcd.base.message.JsonMessage;
import com.bcd.base.security.RSASecurity;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.keys.KeysConst;
import com.bcd.sys.util.ShiroUtil;
import io.swagger.annotations.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;


/**
 * Created by Administrator on 2017/6/15.
 */
@RestController
@RequestMapping("/api/anonymous")
public class AnonymousController {
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPublicKey",method = RequestMethod.GET)
    @ApiOperation(value = "获取公钥",notes = "获取公钥")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "公钥信息")})
    public JsonMessage<Object> getPublicKey(){
        return JsonMessage.success(KeysConst.PUBLIC_KEY_BASE64);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getCookie",method = RequestMethod.GET)
    @ApiOperation(value = "获取cookie",notes = "获取cookie")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "公钥信息")})
    public JsonMessage<Object> getCookie(){
        Subject subject=SecurityUtils.getSubject();
        String cookie=Optional.ofNullable(subject).map(e->e.getSession()).map(e->e.getId()).orElse("").toString();
        return JsonMessage.success(cookie);
    }

    public static void main(String [] args){
        Object[] keys=RSASecurity.generateKey();
        RSAPublicKey publicKey=(RSAPublicKey)keys[0];
        RSAPrivateKey privateKey=(RSAPrivateKey) keys[1];
        System.out.println(Base64.encodeBase64String(publicKey.getEncoded()));
        System.out.println(Base64.encodeBase64String(privateKey.getEncoded()));
    }
}
