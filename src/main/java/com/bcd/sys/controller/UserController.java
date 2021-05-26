package com.bcd.sys.controller;

import com.bcd.base.cache.CacheConst;
import com.bcd.base.cache.anno.LocalCacheable;
import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.UserService;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
@CacheConfig(cacheNames = CacheConst.LOCAL_CACHE, keyGenerator = CacheConst.KEY_GENERATOR)
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表
     *
     * @return
     */
    @LocalCacheable
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description = "查询用户列表")
    @ApiResponse(responseCode = "200", description = "用户列表")
    public JsonMessage<List<UserBean>> list(
            @Parameter(description = "生日开始") @RequestParam(required = false) Date birthdayBegin,
            @Parameter(description = "生日结束") @RequestParam(required = false) Date birthdayEnd,
            @Parameter(description = "身份证号") @RequestParam(required = false) String cardNumber,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "密码") @RequestParam(required = false) String password,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "性别") @RequestParam(required = false) String sex,
            @Parameter(description = "是否可用(0:禁用,1:可用)") @RequestParam(required = false) Integer status,
            @Parameter(description = "类型(1:管理用户,2:企业用户)") @RequestParam(required = false) Integer type,
            @Parameter(description = "用户名") @RequestParam(required = false) String username
    ) {
        Condition condition = Condition.and(
                new DateCondition("birthday", birthdayBegin, DateCondition.Handler.GE),
                new DateCondition("birthday", birthdayEnd, DateCondition.Handler.LE),
                new StringCondition("cardNumber", cardNumber, StringCondition.Handler.ALL_LIKE),
                new StringCondition("email", email, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("password", password, StringCondition.Handler.ALL_LIKE),
                new StringCondition("phone", phone, StringCondition.Handler.ALL_LIKE),
                new StringCondition("realName", realName, StringCondition.Handler.ALL_LIKE),
                new StringCondition("sex", sex, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status", status, NumberCondition.Handler.EQUAL),
                new NumberCondition("type", type, NumberCondition.Handler.EQUAL),
                new StringCondition("username", username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(userService.findAll(condition));
    }

    /**
     * 查询用户分页
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description = "查询用户分页")
    @ApiResponse(responseCode = "200", description = "用户分页结果集")
    public JsonMessage<Page<UserBean>> page(
            @Parameter(description = "生日开始") @RequestParam(required = false) Date birthdayBegin,
            @Parameter(description = "生日结束") @RequestParam(required = false) Date birthdayEnd,
            @Parameter(description = "身份证号") @RequestParam(required = false) String cardNumber,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "密码") @RequestParam(required = false) String password,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "性别") @RequestParam(required = false) String sex,
            @Parameter(description = "是否可用(0:禁用,1:可用)") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "分页参数(页数)") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "分页参数(页大小)") @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        Condition condition = Condition.and(
                new DateCondition("birthday", birthdayBegin, DateCondition.Handler.GE),
                new DateCondition("birthday", birthdayEnd, DateCondition.Handler.LE),
                new StringCondition("cardNumber", cardNumber, StringCondition.Handler.ALL_LIKE),
                new StringCondition("email", email, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("password", password, StringCondition.Handler.ALL_LIKE),
                new StringCondition("phone", phone, StringCondition.Handler.ALL_LIKE),
                new StringCondition("realName", realName, StringCondition.Handler.ALL_LIKE),
                new StringCondition("sex", sex, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("status", status, NumberCondition.Handler.EQUAL),
                new StringCondition("username", username, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(userService.findAll(condition, PageRequest.of(pageNum - 1, pageSize)));
    }

    /**
     * 保存用户
     *
     * @param user
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @Operation(description = "保存用户")
    @ApiResponse(responseCode = "200", description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户实体") @Validated @RequestBody UserBean user) {

        userService.saveUser(user);
        return JsonMessage.success().message("保存成功");
    }

    /**
     * 删除用户
     *
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Operation(description = "删除用户")
    @ApiResponse(responseCode = "200", description = "删除结果")
    public JsonMessage delete(@Parameter(description = "用户id数组") @RequestParam Long[] ids) {
        userService.deleteById(ids);
        return JsonMessage.success().message("删除成功");
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Operation(description = "用户登录")
    @ApiResponse(responseCode = "200", description = "登录的用户信息")
    public JsonMessage<UserBean> login(
            @Parameter(description = "用户名")
            @RequestParam String username,
            @Parameter(description = "密码")
            @RequestParam String password) {
        UserBean user = userService.login(username, password);
        return JsonMessage.success(user);
    }

    /**
     * 注销
     *
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Operation(description = "用户注销")
    @ApiResponse(responseCode = "200", description = "注销结果")
    public JsonMessage<?> logout() {
        Subject currentUser = SecurityUtils.getSubject();
        JsonMessage<?> jsonMessage = JsonMessage.success().message("注销成功");
        //在logout之前必须完成所有与session相关的操作(例如从session中获取国际化的后缀)
        currentUser.logout();
        return jsonMessage;
    }

    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @Operation(description = "重置密码")
    @ApiResponse(responseCode = "200", description = "重制密码结果")
    public JsonMessage<?> resetPassword(@Parameter(description = "用户主键") @RequestParam Long userId) {
        userService.resetPassword(userId);
        return JsonMessage.success().message("重置成功");
    }


    /**
     * 修改密码
     *
     * @param newPassword
     * @param oldPassword
     * @return
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @Operation(description = "修改密码")
    @ApiResponse(responseCode = "200", description = "修改密码结果")
    public JsonMessage updatePassword(
            @Parameter(description = "旧密码")
            @RequestParam String oldPassword,
            @Parameter(description = "新密码")
            @RequestParam String newPassword) {
        UserBean userBean = ShiroUtil.getCurrentUser();
        boolean flag = userService.updatePassword(userBean.getId(), oldPassword, newPassword);
        if (flag) {
            return JsonMessage.success().message("修改成功");
        } else {
            return JsonMessage.fail().message("密码错误");
        }
    }

    /**
     * 授予当前登录用户其他身份
     *
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_runAs)
    @RequestMapping(value = "/runAs", method = RequestMethod.POST)
    @Operation(description = "授予当前登录用户其他身份")
    @ApiResponse(responseCode = "200", description = "授权结果")
    public JsonMessage runAs(@Parameter(description = "要授权身份用户id数组")
                             @RequestParam Long[] ids) {
        userService.runAs(ids);
        return JsonMessage.success().message("授权成功");
    }

    /**
     * 解除当前登录用户的其他身份
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.user_runAs)
    @RequestMapping(value = "/releaseRunAs", method = RequestMethod.POST)
    @Operation(description = "解除当前登录用户的其他身份")
    @ApiResponse(responseCode = "200", description = "解除授权结果")
    public JsonMessage releaseRunAs() {
        userService.releaseRunAs();
        return JsonMessage.success().message("解除授权成功");
    }

}
