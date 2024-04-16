package com.bcd.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.DateCondition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.controller.BaseController;
import com.bcd.base.result.Result;
import com.bcd.base.support_satoken.SaTokenUtil;
import com.bcd.base.support_satoken.anno.NotePermission;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
@Tag(name = "用户-UserController")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(summary = "查询用户列表")
    @ApiResponse(responseCode = "200", description = "用户列表")
    public Result<List<UserBean>> list(
            @Parameter(description = "生日开始", schema = @Schema(type = "integer")) @RequestParam(required = false) Date birthdayBegin,
            @Parameter(description = "生日结束", schema = @Schema(type = "integer")) @RequestParam(required = false) Date birthdayEnd,
            @Parameter(description = "身份证号") @RequestParam(required = false) String cardNumber,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "密码") @RequestParam(required = false) String password,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "性别") @RequestParam(required = false) String sex,
            @Parameter(description = "是否可用(0:禁用,1:可用)") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户名") @RequestParam(required = false) String username
    ) {
        Condition condition = Condition.and(
                DateCondition.BETWEEN("birthday", birthdayBegin, birthdayEnd),
                StringCondition.ALL_LIKE("cardNumber", cardNumber),
                StringCondition.ALL_LIKE("email", email),
                NumberCondition.EQUAL("id", id),
                StringCondition.ALL_LIKE("password", password),
                StringCondition.ALL_LIKE("phone", phone),
                StringCondition.ALL_LIKE("realName", realName),
                StringCondition.ALL_LIKE("sex", sex),
                NumberCondition.EQUAL("status", status),
                StringCondition.ALL_LIKE("username", username)
        );
        return Result.success(userService.list(condition));
    }

    /**
     * 查询用户分页
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.user_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(summary = "查询用户分页")
    @ApiResponse(responseCode = "200", description = "用户分页结果集")
    public Result<Page<UserBean>> page(
            @Parameter(description = "生日开始", schema = @Schema(type = "integer")) @RequestParam(required = false) Date birthdayBegin,
            @Parameter(description = "生日结束", schema = @Schema(type = "integer")) @RequestParam(required = false) Date birthdayEnd,
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
                DateCondition.BETWEEN("birthday", birthdayBegin, birthdayEnd),
                StringCondition.ALL_LIKE("cardNumber", cardNumber),
                StringCondition.ALL_LIKE("email", email),
                NumberCondition.EQUAL("id", id),
                StringCondition.ALL_LIKE("password", password),
                StringCondition.ALL_LIKE("phone", phone),
                StringCondition.ALL_LIKE("realName", realName),
                StringCondition.ALL_LIKE("sex", sex),
                NumberCondition.EQUAL("status", status),
                StringCondition.ALL_LIKE("username", username)
        );
        return Result.success(userService.page(condition, PageRequest.of(pageNum - 1, pageSize)));
    }

    /**
     * 保存用户
     *
     * @param user
     * @return
     */
    @SaCheckNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @Operation(summary = "保存用户")
    @ApiResponse(responseCode = "200", description = "保存结果")
    public Result save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户实体") @Validated @RequestBody UserBean user) {
        userService.saveUser(user);
        return Result.success().message("保存成功");
    }

    /**
     * 删除用户
     *
     * @param ids
     * @return
     */
    @SaCheckNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Operation(summary = "删除用户")
    @ApiResponse(responseCode = "200", description = "删除结果")
    public Result delete(@Parameter(description = "用户id数组", example = "100,101,102") @RequestParam long[] ids) {
        userService.delete(ids);
        return Result.success().message("删除成功");
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Operation(summary = "用户登录")
    @ApiResponse(responseCode = "200", description = "登录的用户信息")
    public Result<UserBean> login(
            @Parameter(description = "用户名")
            @RequestParam String username,
            @Parameter(description = "密码")
            @RequestParam String password) {
        UserBean user = userService.login(username, password);
        return Result.success(user);
    }

    /**
     * 注销
     *
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Operation(summary = "用户注销")
    @ApiResponse(responseCode = "200", description = "注销结果")
    public Result<?> logout() {
        Result<?> result = Result.success().message("注销成功");
        //在logout之前必须完成所有与session相关的操作(例如从session中获取国际化的后缀)
        StpUtil.logout();
        return result;
    }

    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
    @SaCheckNotePermissions(NotePermission.user_edit)
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @Operation(summary = "重置密码")
    @ApiResponse(responseCode = "200", description = "重制密码结果")
    public Result<?> resetPassword(@Parameter(description = "用户主键") @RequestParam Long userId) {
        userService.resetPassword(userId);
        return Result.success().message("重置成功");
    }


    /**
     * 修改密码
     *
     * @param newPassword
     * @param oldPassword
     * @return
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @Operation(summary = "修改密码")
    @ApiResponse(responseCode = "200", description = "修改密码结果")
    public Result updatePassword(
            @Parameter(description = "旧密码")
            @RequestParam String oldPassword,
            @Parameter(description = "新密码")
            @RequestParam String newPassword) {
        UserBean userBean = (UserBean)SaTokenUtil.getLoginUser_cache();
        boolean flag = userService.updatePassword(userBean.id, oldPassword, newPassword);
        if (flag) {
            return Result.success().message("修改成功");
        } else {
            return Result.fail().message("密码错误");
        }
    }
}
