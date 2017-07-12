package com.controller;

import com.base.json.JsonMessage;
import com.base.security.RSASecurity;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Administrator on 2017/6/15.
 */
@RestController
@RequestMapping("/api/security")
public class SecurityController {
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPublicKey",method = RequestMethod.GET)
    @ApiOperation(value = "获取公钥",notes = "获取公钥")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "公钥信息")})
    public JsonMessage<Object> getPublicKey(){
        return JsonMessage.successed(RSASecurity.keyMap.get(RSASecurity.PUBLIC_KEY));
    }
}
