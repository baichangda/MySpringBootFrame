package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.MessageDefine;
import com.bcd.sys.service.UserService;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
@Api(tags = "用户管理/UserController")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表
     * @return
     */
    @Cacheable
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户列表")
    @ApiResponse(code = 200,message = "用户列表")
    public JsonMessage<List<UserBean>> list(
            @ApiParam(value = "生日开始") @RequestParam(value = "birthdayBegin", required = false) Date birthdayBegin,
            @ApiParam(value = "生日结束") @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "身份证号") @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @ApiParam(value = "邮箱") @RequestParam(value = "email", required = false) String email,
            @ApiParam(value = "主键") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "密码") @RequestParam(value = "password", required = false) String password,
            @ApiParam(value = "手机号") @RequestParam(value = "phone", required = false) String phone,
            @ApiParam(value = "真实姓名") @RequestParam(value = "realName", required = false) String realName,
            @ApiParam(value = "性别") @RequestParam(value = "sex", required = false) String sex,
            @ApiParam(value = "是否可用(0:禁用,1:可用)") @RequestParam(value = "status", required = false) Integer status,
            @ApiParam(value = "类型(1:管理用户,2:企业用户)") @RequestParam(value = "type", required = false) Integer type,
            @ApiParam(value = "用户名") @RequestParam(value = "username", required = false) String username
    ){
        Condition condition= Condition.and(
                new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
                new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
                new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
                new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
                new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
                new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
                new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
                new NumberCondition("type",type, NumberCondition.Handler.EQUAL),
                new StringCondition("username",username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success().withData(userService.findAll(condition));
    }

    /**
     * 查询用户分页
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询用户分页",notes = "查询用户分页")
    @ApiResponse(code = 200,message = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
            @ApiParam(value = "生日开始") @RequestParam(value = "birthdayBegin", required = false) Date birthdayBegin,
            @ApiParam(value = "生日结束") @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
            @ApiParam(value = "身份证号") @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @ApiParam(value = "邮箱") @RequestParam(value = "email", required = false) String email,
            @ApiParam(value = "主键") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "密码") @RequestParam(value = "password", required = false) String password,
            @ApiParam(value = "手机号") @RequestParam(value = "phone", required = false) String phone,
            @ApiParam(value = "真实姓名") @RequestParam(value = "realName", required = false) String realName,
            @ApiParam(value = "性别") @RequestParam(value = "sex", required = false) String sex,
            @ApiParam(value = "是否可用(0:禁用,1:可用)") @RequestParam(value = "status", required = false) Integer status,
            @ApiParam(value = "用户名") @RequestParam(value = "username", required = false) String username,
            @ApiParam(value = "分页参数(页数)",defaultValue = "1")  @RequestParam(value = "pageNum",required = false,defaultValue = "1")Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",defaultValue = "20") @RequestParam(value = "pageSize",required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
                new DateCondition("birthday",birthdayBegin, DateCondition.Handler.GE),
                new DateCondition("birthday",birthdayEnd, DateCondition.Handler.LE),
                new StringCondition("cardNumber",cardNumber, StringCondition.Handler.ALL_LIKE),
                new StringCondition("email",email, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("password",password, StringCondition.Handler.ALL_LIKE),
                new StringCondition("phone",phone, StringCondition.Handler.ALL_LIKE),
                new StringCondition("realName",realName, StringCondition.Handler.ALL_LIKE),
                new StringCondition("sex",sex, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status",status, NumberCondition.Handler.EQUAL),
                new StringCondition("username",username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success().withData(userService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存用户
     * @param user
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存用户",notes = "保存用户")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "用户实体") @Validated @RequestBody UserBean user){

        userService.saveUser(user);
        return com.bcd.base.define.MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }

    /**
     * 删除用户
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户",notes = "删除用户")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "用户id数组") @RequestParam Long[] ids){
        userService.deleteById(ids);
        return com.bcd.base.define.MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value="用户登录",notes = "根据帐号密码登录")
    @ApiResponse(code = 200,message = "登录的用户信息")
    public JsonMessage login(
            @ApiParam(value = "用户名")
            @RequestParam(value = "username",required = true) String username,
            @ApiParam(value = "密码")
            @RequestParam(value = "password",required = true) String password){
        UserBean user= userService.login(username,password);
        return JsonMessage.success().withData(user);
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
        JsonMessage jsonMessage= MessageDefine.SUCCESS_LOGOUT.toJsonMessage(true);
        //在logout之前必须完成所有与session相关的操作(例如从session中获取国际化的后缀)
        currentUser.logout();
        return jsonMessage;
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ApiOperation(value="重置密码",notes = "重置密码")
    @ApiResponse(code = 200,message = "重制密码结果")
    public JsonMessage resetPassword(@ApiParam(value = "用户主键",example = "1") @RequestParam(value = "userId") Long userId){
        userService.resetPassword(userId);
        return MessageDefine.SUCCESS_RESET_PASSWORD.toJsonMessage(true);
    }


    /**
     * 修改密码
     * @param newPassword
     * @param oldPassword
     * @return
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ApiOperation(value="修改密码",notes = "修改密码")
    @ApiResponse(code = 200,message = "修改密码结果")
    public JsonMessage updatePassword(
            @ApiParam(value = "旧密码")
            @RequestParam(value = "oldPassword") String oldPassword,
            @ApiParam(value = "新密码")
            @RequestParam(value = "newPassword") String newPassword){
        UserBean userBean= ShiroUtil.getCurrentUser();
        boolean flag= userService.updatePassword(userBean.getId(),oldPassword,newPassword);
        if(flag){
            return com.bcd.base.define.MessageDefine.SUCCESS_UPDATE.toJsonMessage(true);
        }else{
            return MessageDefine.ERROR_PASSWORD_WRONG.toJsonMessage();
        }
    }

    /**
     * 授予当前登录用户其他身份
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_runAs)
    @RequestMapping(value = "/runAs", method = RequestMethod.POST)
    @ApiOperation(value="授予当前登录用户其他身份",notes = "授予当前登录用户其他身份")
    @ApiResponse(code = 200,message = "授权结果")
    public JsonMessage runAs(Long ... ids){
        userService.runAs(ids);
        return MessageDefine.SUCCESS_AUTHORITY.toJsonMessage(true);
    }

    /**
     * 解除当前登录用户的其他身份
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_runAs)
    @RequestMapping(value = "/releaseRunAs", method = RequestMethod.POST)
    @ApiOperation(value="解除当前登录用户的其他身份",notes = "解除当前登录用户的其他身份")
    @ApiResponse(code = 200,message = "解除授权结果")
    public JsonMessage releaseRunAs(){
        userService.releaseRunAs();
        return MessageDefine.SUCCESS_RELEASE.toJsonMessage(true);
    }

}
