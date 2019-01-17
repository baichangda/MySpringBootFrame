package com.bcd.sys.mongodb.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.mongodb.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.mongodb.bean.UserBean;
import com.bcd.sys.mongodb.service.UserService;

@SuppressWarnings(value = "unchecked")
//@RestController
@RequestMapping("/api/sys/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;


    /**
     * 查询用户列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户列表")
    @ApiResponse(code = 200,message = "用户列表")
    public JsonMessage<List<UserBean>> list(
            @ApiParam(value = "生日开始")
            @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
            @ApiParam(value = "生日截止")
            @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "性别")
            @RequestParam(value = "sex",required = false) String sex,
            @ApiParam(value = "真实名称")
            @RequestParam(value = "realName",required = false) String realName,
            @ApiParam(value = "密码")
            @RequestParam(value = "password",required = false) String password,
            @ApiParam(value = "手机号")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id,
            @ApiParam(value = "身份证")
            @RequestParam(value = "cardNumber",required = false) String cardNumber,
            @ApiParam(value = "邮箱")
            @RequestParam(value = "email",required = false) String email,
            @ApiParam(value = "状态",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = false) String username
            ){
        Condition condition= Condition.and(
            new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
            new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
            new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
            new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
            new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL),
            new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
            new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
            new StringCondition("username",username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(userService.findAll(condition));
    }


    /**
     * 查询用户列表
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询用户分页",notes = "查询用户分页")
    @ApiResponse(code = 200,message = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
            @ApiParam(value = "生日开始")
            @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
            @ApiParam(value = "生日截止")
            @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "性别")
            @RequestParam(value = "sex",required = false) String sex,
            @ApiParam(value = "真实名称")
            @RequestParam(value = "realName",required = false) String realName,
            @ApiParam(value = "密码")
            @RequestParam(value = "password",required = false) String password,
            @ApiParam(value = "手机号")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "主键")
            @RequestParam(value = "id",required = false) String id,
            @ApiParam(value = "身份证")
            @RequestParam(value = "cardNumber",required = false) String cardNumber,
            @ApiParam(value = "邮箱")
            @RequestParam(value = "email",required = false) String email,
            @ApiParam(value = "状态",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = false) String username,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
            ){
        Condition condition= Condition.and(
            new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
            new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
            new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
            new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
            new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("id",id, StringCondition.Handler.EQUAL),
            new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
            new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
            new StringCondition("username",username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(userService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));

    }

    /**
     * 保存用户
     * @param user
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存用户",notes = "保存用户")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "用户实体")  @RequestBody UserBean user){
        userService.save(user);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除用户
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户",notes = "删除用户")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "用户id数组") @RequestParam String[] ids){
        userService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }
}
