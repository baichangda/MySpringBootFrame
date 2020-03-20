package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.define.MessageDefine;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.UserService;

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
    @Cacheable
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "type", value = "类型(1:管理用户,2:企业用户)", dataType = "String"),
        @ApiImplicitParam(name = "orgCode", value = "关联机构编码", dataType = "String"),
        @ApiImplicitParam(name = "username", value = "用户名", dataType = "String"),
        @ApiImplicitParam(name = "password", value = "密码", dataType = "String"),
        @ApiImplicitParam(name = "email", value = "邮箱", dataType = "String"),
        @ApiImplicitParam(name = "phone", value = "手机号", dataType = "String"),
        @ApiImplicitParam(name = "realName", value = "真实姓名", dataType = "String"),
        @ApiImplicitParam(name = "sex", value = "性别", dataType = "String"),
        @ApiImplicitParam(name = "birthdayBegin", value = "生日开始", dataType = "String"),
        @ApiImplicitParam(name = "birthdayEnd", value = "生日结束", dataType = "String"),
        @ApiImplicitParam(name = "cardNumber", value = "身份证号", dataType = "String"),
        @ApiImplicitParam(name = "status", value = "是否可用(0:禁用,1:可用)", dataType = "String")
    })
    @ApiResponse(code = 200,message = "用户列表")
    public JsonMessage<List<UserBean>> list(
        @RequestParam(value = "id",required = false) Long id,
        @RequestParam(value = "orgCode",required = false) String orgCode,
        @RequestParam(value = "username",required = false) String username,
        @RequestParam(value = "password",required = false) String password,
        @RequestParam(value = "email",required = false) String email,
        @RequestParam(value = "phone",required = false) String phone,
        @RequestParam(value = "realName",required = false) String realName,
        @RequestParam(value = "sex",required = false) String sex,
        @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
        @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
        @RequestParam(value = "cardNumber",required = false) String cardNumber,
        @RequestParam(value = "status",required = false) Integer status
    ){
        UserBean curUser= ShiroUtil.getCurrentUser();
        orgCode=curUser.getType()==1?orgCode:curUser.getOrgCode();
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("orgCode",orgCode, StringCondition.Handler.LEFT_LIKE),
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
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询用户列表",notes = "查询用户分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "主键", dataType = "String"),
        @ApiImplicitParam(name = "type", value = "类型(1:管理用户,2:企业用户)", dataType = "String"),
        @ApiImplicitParam(name = "orgCode", value = "关联机构编码", dataType = "String"),
        @ApiImplicitParam(name = "username", value = "用户名", dataType = "String"),
        @ApiImplicitParam(name = "password", value = "密码", dataType = "String"),
        @ApiImplicitParam(name = "email", value = "邮箱", dataType = "String"),
        @ApiImplicitParam(name = "phone", value = "手机号", dataType = "String"),
        @ApiImplicitParam(name = "realName", value = "真实姓名", dataType = "String"),
        @ApiImplicitParam(name = "sex", value = "性别", dataType = "String"),
        @ApiImplicitParam(name = "birthdayBegin", value = "生日开始", dataType = "String"),
        @ApiImplicitParam(name = "birthdayEnd", value = "生日结束", dataType = "String"),
        @ApiImplicitParam(name = "cardNumber", value = "身份证号", dataType = "String"),
        @ApiImplicitParam(name = "status", value = "是否可用(0:禁用,1:可用)", dataType = "String"),
        @ApiImplicitParam(name = "pageNum", value = "分页参数(页数)", dataType = "String"),
        @ApiImplicitParam(name = "pageSize", value = "分页参数(页大小)", dataType = "String")
    })
    @ApiResponse(code = 200,message = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
        @RequestParam(value = "id",required = false) Long id,
        @RequestParam(value = "orgCode",required = false) String orgCode,
        @RequestParam(value = "username",required = false) String username,
        @RequestParam(value = "password",required = false) String password,
        @RequestParam(value = "email",required = false) String email,
        @RequestParam(value = "phone",required = false) String phone,
        @RequestParam(value = "realName",required = false) String realName,
        @RequestParam(value = "sex",required = false) String sex,
        @RequestParam(value = "birthdayBegin",required = false) Date birthdayBegin,
        @RequestParam(value = "birthdayEnd",required = false) Date birthdayEnd,
        @RequestParam(value = "cardNumber",required = false) String cardNumber,
        @RequestParam(value = "status",required = false) Integer status,
        @RequestParam(value = "pageNum",required = false)Integer pageNum,
        @RequestParam(value = "pageSize",required = false) Integer pageSize
    ){
        UserBean curUser= ShiroUtil.getCurrentUser();
        orgCode=curUser.getType()==1?orgCode:curUser.getOrgCode();
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("orgCode",orgCode, StringCondition.Handler.LEFT_LIKE),
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
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存用户",notes = "保存用户")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "用户实体") @RequestBody UserBean user){
        if(user.getId()==null){
            user.setPassword(userService.encryptPassword(user.getUsername(), CommonConst.INITIAL_PASSWORD));
        }
        userService.save(user);
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
