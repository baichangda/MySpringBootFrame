package com.bcd.base.support_jpa.code.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.base.support_jpa.code.bean.UserBean;
import com.bcd.base.support_jpa.code.service.UserService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/code/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description="查询用户列表")
    @ApiResponse(responseCode = "200",description = "用户列表")
    public JsonMessage<List<UserBean>> list(
        @Parameter(description = "生日开始") @RequestParam(required = false) Date birthdayBegin,
        @Parameter(description = "生日结束") @RequestParam(required = false) Date birthdayEnd,
        @Parameter(description = "身份证号") @RequestParam(required = false) String cardNumber,
        @Parameter(description = "创建时间开始") @RequestParam(required = false) Date createTimeBegin,
        @Parameter(description = "创建时间结束") @RequestParam(required = false) Date createTimeEnd,
        @Parameter(description = "创建人id") @RequestParam(required = false) Long createUserId,
        @Parameter(description = "创建人姓名") @RequestParam(required = false) String createUserName,
        @Parameter(description = "邮箱") @RequestParam(required = false) String email,
        @Parameter(description = "主键") @RequestParam(required = false) Long id,
        @Parameter(description = "密码") @RequestParam(required = false) String password,
        @Parameter(description = "手机号") @RequestParam(required = false) String phone,
        @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
        @Parameter(description = "性别") @RequestParam(required = false) String sex,
        @Parameter(description = "是否可用(0:禁用,1:可用)") @RequestParam(required = false) Integer status,
        @Parameter(description = "更新时间开始") @RequestParam(required = false) Date updateTimeBegin,
        @Parameter(description = "更新时间结束") @RequestParam(required = false) Date updateTimeEnd,
        @Parameter(description = "更新人id") @RequestParam(required = false) Long updateUserId,
        @Parameter(description = "更新人姓名") @RequestParam(required = false) String updateUserName,
        @Parameter(description = "用户名") @RequestParam(required = false) String username
    ){
        Condition condition= Condition.and(
           new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
           new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
           new StringCondition("cardNumber",cardNumber),
           new DateCondition("createTime",createTimeBegin, DateCondition.Handler.GE),
           new DateCondition("createTime",createTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("createUserId",createUserId),
           new StringCondition("createUserName",createUserName),
           new StringCondition("email",email),
           new NumberCondition("id",id),
           new StringCondition("password",password),
           new StringCondition("phone",phone),
           new StringCondition("realName",realName),
           new StringCondition("sex",sex),
           new NumberCondition("status",status),
           new DateCondition("updateTime",updateTimeBegin, DateCondition.Handler.GE),
           new DateCondition("updateTime",updateTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("updateUserId",updateUserId),
           new StringCondition("updateUserName",updateUserName),
           new StringCondition("username",username)
        );
        return JsonMessage.success(userService.findAll(condition));
    }

    /**
     * 查询用户分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description="查询用户分页")
    @ApiResponse(responseCode = "200",description = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
        @Parameter(description = "生日开始") @RequestParam(required = false) Date birthdayBegin,
        @Parameter(description = "生日结束") @RequestParam(required = false) Date birthdayEnd,
        @Parameter(description = "身份证号") @RequestParam(required = false) String cardNumber,
        @Parameter(description = "创建时间开始") @RequestParam(required = false) Date createTimeBegin,
        @Parameter(description = "创建时间结束") @RequestParam(required = false) Date createTimeEnd,
        @Parameter(description = "创建人id") @RequestParam(required = false) Long createUserId,
        @Parameter(description = "创建人姓名") @RequestParam(required = false) String createUserName,
        @Parameter(description = "邮箱") @RequestParam(required = false) String email,
        @Parameter(description = "主键") @RequestParam(required = false) Long id,
        @Parameter(description = "密码") @RequestParam(required = false) String password,
        @Parameter(description = "手机号") @RequestParam(required = false) String phone,
        @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
        @Parameter(description = "性别") @RequestParam(required = false) String sex,
        @Parameter(description = "是否可用(0:禁用,1:可用)") @RequestParam(required = false) Integer status,
        @Parameter(description = "更新时间开始") @RequestParam(required = false) Date updateTimeBegin,
        @Parameter(description = "更新时间结束") @RequestParam(required = false) Date updateTimeEnd,
        @Parameter(description = "更新人id") @RequestParam(required = false) Long updateUserId,
        @Parameter(description = "更新人姓名") @RequestParam(required = false) String updateUserName,
        @Parameter(description = "用户名") @RequestParam(required = false) String username,
        @Parameter(description = "分页参数(页数)")  @RequestParam(required = false,defaultValue = "1")Integer pageNum,
        @Parameter(description = "分页参数(页大小)") @RequestParam(required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
           new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
           new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
           new StringCondition("cardNumber",cardNumber),
           new DateCondition("createTime",createTimeBegin, DateCondition.Handler.GE),
           new DateCondition("createTime",createTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("createUserId",createUserId),
           new StringCondition("createUserName",createUserName),
           new StringCondition("email",email),
           new NumberCondition("id",id),
           new StringCondition("password",password),
           new StringCondition("phone",phone),
           new StringCondition("realName",realName),
           new StringCondition("sex",sex),
           new NumberCondition("status",status),
           new DateCondition("updateTime",updateTimeBegin, DateCondition.Handler.GE),
           new DateCondition("updateTime",updateTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("updateUserId",updateUserId),
           new StringCondition("updateUserName",updateUserName),
           new StringCondition("username",username)
        );
        return JsonMessage.success(userService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存用户
     * @param user
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @Operation(description = "保存用户")
    @ApiResponse(responseCode = "200",description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户实体") @Validated @RequestBody UserBean user){
        userService.save(user);
        return JsonMessage.success();
    }


    /**
     * 删除用户
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @Operation(description = "删除用户")
    @ApiResponse(responseCode = "200",description = "删除结果")
    public JsonMessage delete(@Parameter(description = "用户id数组") @RequestParam Long[] ids){
        userService.deleteAllById(ids);
        return JsonMessage.success();
    }

}
