package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.define.ErrorDefine;
import io.swagger.annotations.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.UserService;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;



    /**
     * 查询用户列表
     * @return
     */
    @RequiresPermissions("a")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户列表")
    @ApiResponse(code = 200,message = "用户列表")
    public JsonMessage<List<UserBean>> list(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "关联机构id",example="1")
            @RequestParam(value = "orgId",required = false) Long orgId,
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = false) String username,
            @ApiParam(value = "用户名")
            @RequestParam(value = "password",required = false) String password,
            @ApiParam(value = "邮箱")
            @RequestParam(value = "email",required = false) String email,
            @ApiParam(value = "手机号")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "真实姓名")
            @RequestParam(value = "realName",required = false) String realName,
            @ApiParam(value = "性别")
            @RequestParam(value = "sex",required = false) String sex,
            @ApiParam(value = "生日开始")
            @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
            @ApiParam(value = "生日截止")
            @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "身份证号")
            @RequestParam(value = "cardNumber",required = false) String cardNumber,
            @ApiParam(value = "是否可用（0:禁用,1:可用）",example="1")
            @RequestParam(value = "status",required = false) Integer status
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("orgId",orgId, NumberCondition.Handler.EQUAL),
            new StringCondition("username",username, StringCondition.Handler.ALL_LIKE),
            new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
            new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
            new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
            new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
            new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
            new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(userService.findAll(condition));
    }

    /**
     * 查询用户分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户分页")
    @ApiResponse(code = 200,message = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "关联机构id",example="1")
            @RequestParam(value = "orgId",required = false) Long orgId,
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = false) String username,
            @ApiParam(value = "用户名")
            @RequestParam(value = "password",required = false) String password,
            @ApiParam(value = "邮箱")
            @RequestParam(value = "email",required = false) String email,
            @ApiParam(value = "手机号")
            @RequestParam(value = "phone",required = false) String phone,
            @ApiParam(value = "真实姓名")
            @RequestParam(value = "realName",required = false) String realName,
            @ApiParam(value = "性别")
            @RequestParam(value = "sex",required = false) String sex,
            @ApiParam(value = "生日开始")
            @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
            @ApiParam(value = "生日截止")
            @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "身份证号")
            @RequestParam(value = "cardNumber",required = false) String cardNumber,
            @ApiParam(value = "是否可用（0:禁用,1:可用）",example="1")
            @RequestParam(value = "status",required = false) Integer status,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new NumberCondition("orgId",orgId, NumberCondition.Handler.EQUAL),
            new StringCondition("username",username, StringCondition.Handler.ALL_LIKE),
            new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
            new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
            new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
            new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
            new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
            new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
            new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
            new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("status",status, NumberCondition.Handler.EQUAL)
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
    public JsonMessage save(@ApiParam(value = "用户实体") @RequestBody @Validated UserBean user){
        if(user.getId()==null){
            user.setPassword(userService.encryptPassword(user.getUsername(),user.getPassword()));
        }
        userService.save(user);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除用户
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户",notes = "删除用户")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "用户id数组") @RequestParam Long[] ids){
        userService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

    /**
     * 登录
     * @param username
     * @param password
     * @param timeZone
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value="用户登录",notes = "根据帐号密码登录")
    @ApiResponse(code = 200,message = "登录的用户信息")
    public JsonMessage login(
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = true) String username,
            @ApiParam(value = "密码")
            @RequestParam(value = "password",required = true) String password,
            @ApiParam(value = "时区")
            @RequestParam(value="timeZone",required = true)String timeZone){
        UserBean user= userService.login(username,password,timeZone);
        return JsonMessage.success(user);
    }

    /**
     * 注销
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ApiOperation(value="用户注销",notes = "用户注销")
    @ApiResponse(code = 200,message = "注销结果")
    public JsonMessage logout() {
        Subject currentUser = SecurityUtils.getSubject();
        JsonMessage jsonMessage= com.bcd.sys.define.SuccessDefine.SUCCESS_LOGOUT.toJsonMessage();
        //在logout之前必须完成所有与session相关的操作(例如从session中获取国际化的后缀)
        currentUser.logout();
        return jsonMessage;
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ApiOperation(value="重置密码",notes = "重置密码")
    @ApiResponse(code = 200,message = "重制密码结果")
    public JsonMessage resetPassword(@ApiParam(value = "用户主键",example = "1") @RequestParam(value = "userId") Long userId){
        userService.resetPassword(userId);
        return com.bcd.sys.define.SuccessDefine.SUCCESS_RESET_PASSWORD.toJsonMessage();
    }


    /**
     * 修改密码
     * @param userId
     * @return
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ApiOperation(value="修改密码",notes = "修改密码")
    @ApiResponse(code = 200,message = "修改密码结果")
    public JsonMessage updatePassword(
            @ApiParam(value = "用户主键",example = "1")
            @RequestParam(value = "userId") Long userId,
            @ApiParam(value = "旧密码")
            @RequestParam(value = "oldPassword") String oldPassword,
            @ApiParam(value = "新密码")
            @RequestParam(value = "newPassword") String newPassword){
        boolean flag= userService.updatePassword(userId,oldPassword,newPassword);
        if(flag){
            return SuccessDefine.SUCCESS_UPDATE.toJsonMessage();
        }else{
            return ErrorDefine.ERROR_PASSWORD_WRONG.toJsonMessage();
        }
    }

    /**
     * 授予当前登录用户其他身份
     * @param ids
     * @return
     */
    @RequestMapping(value = "/runAs", method = RequestMethod.POST)
    @ApiOperation(value="授予当前登录用户其他身份",notes = "授予当前登录用户其他身份")
    @ApiResponse(code = 200,message = "授权结果")
    public JsonMessage runAs(Long ... ids){
        userService.runAs(ids);
        return com.bcd.sys.define.SuccessDefine.SUCCESS_AUTHORITY.toJsonMessage();
    }

    /**
     * 解除当前登录用户的其他身份
     * @return
     */
    @RequestMapping(value = "/releaseRunAs", method = RequestMethod.POST)
    @ApiOperation(value="解除当前登录用户的其他身份",notes = "解除当前登录用户的其他身份")
    @ApiResponse(code = 200,message = "解除授权结果")
    public JsonMessage releaseRunAs(){
        userService.releaseRunAs();
        return com.bcd.sys.define.SuccessDefine.SUCCESS_RELEASE.toJsonMessage();
    }

}
