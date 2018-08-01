package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.json.jackson.filter.SimpleFilterBean;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.I18nUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.FilterUtil;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.service.UserService;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

/**
 * @author acemma
 * Created by Administrator on 2017/4/18.
 */
@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     * @param username
     * @param password
     * @param timeZone
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value="用户登录",notes = "根据帐号密码登录")
    @ApiResponses(value={@ApiResponse(code=200,message = "是否登录成功")})
    public MappingJacksonValue login(@ApiParam(value = "用户名") @RequestParam(value = "username",required = true) String username,
                                     @ApiParam(value = "密码") @RequestParam(value = "password",required = true) String password,
                                     @ApiParam(value = "时区") @RequestParam(value="timeZone",required = true)String timeZone){

        UserBean user= userService.login(username,password,timeZone);
        SimpleFilterBean[] filters= FilterUtil.getOneDeepJsonFilter(UserBean.class);
        return JsonMessage.success(user).toMappingJacksonValue(filters);
    }

    /**
     * 注销
     * @return
     */


    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ApiOperation(value="用户注销",notes = "用户注销")
    @ApiResponses(value={@ApiResponse(code=200,message = "是否注销成功")})
    public JsonMessage logout() {
        Subject currentUser = SecurityUtils.getSubject();
        String successMsg=I18nUtil.getMessage("UserController.logout.SUCCESSED");
        //在logout之前必须完成所有与session相关的操作(例如从session中获取国际化的后缀)
        currentUser.logout();
        return new JsonMessage(true, successMsg);
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ApiOperation(value="重置密码",notes = "重置密码")
    @ApiResponses(value={@ApiResponse(code=200,message = "是否重置成功")})
    public JsonMessage resetPassword(@ApiParam(value = "用户主键",example = "1") @RequestParam(value = "userId") Long userId){
        userService.resetPassword(userId);
        return new JsonMessage(true,I18nUtil.getMessage("UserController.resetPassword.SUCCESSED"));
    }


    /**
     * 修改密码
     * @param userId
     * @return
     */

    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ApiOperation(value="修改密码",notes = "修改密码")
    @ApiResponses(value={@ApiResponse(code=200,message = "是否修改成功")})
    public JsonMessage updatePassword(@ApiParam(value = "用户主键",example = "1") @RequestParam(value = "userId") Long userId,
                                      @ApiParam(value = "旧密码") @RequestParam(value = "oldPassword") String oldPassword,
                                      @ApiParam(value = "新密码") @RequestParam(value = "newPassword") String newPassword){
        boolean flag= userService.updatePassword(userId,oldPassword,newPassword);
        if(flag){
            return new JsonMessage(true,I18nUtil.getMessage("COMMON.UPDATE_SUCCESSED"));
        }else{
            return new JsonMessage(false,I18nUtil.getMessage("UserController.updatePassword.passwordWrong"));
        }


    }

    /**
     * 查询用户列表
     * @param username
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询所有用户",notes = "查询所有用户")
    @ApiResponses(value={@ApiResponse(code=200,response = JsonMessage.class,message = "所有用户列表")})
    public MappingJacksonValue list(
            @ApiParam(value = "用户主键",example = "1") @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "用户名") @RequestParam(value = "username",required = false) String username,
            @ApiParam(value = "组织机构名称") @RequestParam(value = "orgName",required = false) String orgName,
            @ApiParam(value = "分页参数(页数)",example="1") @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20") @RequestParam(value = "pageSize",required = false) Integer pageSize){
        SimpleFilterBean[] filters= FilterUtil.getZeroDeepJsonFilter(UserBean.class);
        Condition condition= Condition.and(
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("username",username, StringCondition.Handler.ALL_LIKE),
                new StringCondition("orgBean.name",orgName, StringCondition.Handler.ALL_LIKE)
        );
        if(pageNum==null||pageSize==null){
            return JsonMessage.success(userService.findAll(condition)).toMappingJacksonValue(filters);
        }else{
            return JsonMessage.success(userService.findAll(condition,PageRequest.of(pageNum-1,pageSize))).toMappingJacksonValue(filters);
        }

    }

    /**
     * 保存用户
     * @param user
     * @return
     */

    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存用户",notes = "保存用户")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存用户")})
    public JsonMessage save(@ApiParam(value = "用户实体")@RequestBody UserBean user){
        //如果为新增操作、则赋予初始密码
        if(user.getId()==null){
            user.setPassword(new Md5Hash(CommonConst.INITIAL_PASSWORD,user.getUsername()).toBase64());
        }
        userService.saveIgnoreNull(user);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除用户
     * @param userIdArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户",notes = "删除用户")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除用户")})
    public JsonMessage delete(@ApiParam(value = "用户主键数组")@RequestParam Long[] userIdArr){
        userService.deleteById(userIdArr);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
