package com.sys.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.base.condition.BaseCondition;
import com.base.condition.impl.NumberCondition;
import com.base.condition.impl.StringCondition;
import com.base.controller.BaseController;
import com.base.json.JsonMessage;
import com.base.util.I18nUtil;
import com.base.util.JsonUtil;
import com.sys.bean.RoleBean;
import com.sys.bean.UserBean;
import com.sys.service.UserService;
import com.sys.util.ShiroUtil;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author acemma
 * Created by Administrator on 2017/4/18.
 */
@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/user")
public class UserController extends BaseController {

    private final String initialPassword="123qwe";

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
    @ApiImplicitParams({
            @ApiImplicitParam( name = "username", value = "帐号", dataType = "String",paramType = "query"),
            @ApiImplicitParam( name = "password", value = "密码", dataType = "String",paramType = "query"),
            @ApiImplicitParam( name = "timeZone", value = "时区", dataType = "String",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "是否登录成功")})
    public JsonMessage login(@RequestParam(value = "username",required = true) String username,
                             @RequestParam(value = "password",required = true) String password,
                             @RequestParam(value="timeZone",required = true)String timeZone){

            UserBean user= userService.login(username,password,timeZone);
            SimplePropertyPreFilter[] filters=JsonUtil.getOneDeepJsonFilter(UserBean.class);
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(user,filters));
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
        currentUser.logout();
        return new JsonMessage(true, I18nUtil.getMessage("UserController.logout.SUCCESSED"));
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ApiOperation(value="重置密码",notes = "重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam( name = "userId", value = "用户主键", dataType = "Long",paramType = "query"),
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "是否重置成功")})
    public JsonMessage resetPassword(@RequestParam(value = "userId") Long userId){
        //1、重置密码
        UserBean sysUserDTO= userService.findOne(userId);
        //2、设置默认密码
        sysUserDTO.setPassword(new Md5Hash(initialPassword,sysUserDTO.getUsername()).toBase64());
        userService.save(sysUserDTO);

        return new JsonMessage(true,I18nUtil.getMessage("UserController.resetPassword.SUCCESSED"));
    }


    /**
     * 修改密码
     * @param userId
     * @return
     */

    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ApiOperation(value="修改密码",notes = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam( name = "userId", value = "用户主键", dataType = "Long",paramType = "query",required = true),
            @ApiImplicitParam( name = "oldPassword", value = "旧密码(已经加密)", dataType = "String",paramType = "query",required = true),
            @ApiImplicitParam( name = "newPassword", value = "新密码(已经加密)", dataType = "String",paramType = "query",required = true)
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "是否修改成功")})
    public JsonMessage updatePassword(@RequestParam(value = "userId") Long userId,
                                      @RequestParam(value = "oldPassword") String oldPassword,
                                      @RequestParam(value = "newPassword") String newPassword){
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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "主键", dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "username",value = "用户名", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "orgName",value = "机构名称", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "pageNum",value = "当前页数(分页参数)",dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页显示记录数(分页参数)",dataType = "int",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "所有用户列表")})
    public JsonMessage<String> list(
            @RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "username",required = false) String username,
            @RequestParam(value = "orgName",required = false) String orgName,
            @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
            @RequestParam(value = "pageSize",defaultValue = "20") Integer pageSize){
        SimplePropertyPreFilter [] filters=JsonUtil.getOneDeepJsonFilter(UserBean.class);
        BaseCondition condition= BaseCondition.and(
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("username",username, StringCondition.Handler.ALL_LIKE),
                new StringCondition("org.name",orgName, StringCondition.Handler.ALL_LIKE)
        );
        if(pageNum==null||pageSize==null){
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(userService.findAll(condition),filters));
        }else{
            return JsonMessage.successed(JsonUtil.toDefaultJSONString(userService.findAll(condition,new PageRequest(pageNum-1,pageSize)),filters));
        }

    }

    /**
     * 保存用户
     * @param user
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存用户",notes = "保存用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user",value = "用户实体",dataType = "SysUserDTO",paramType = "body"),
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存用户")})
    public JsonMessage<String> save(@RequestBody UserBean user){
        //如果为新增操作、则赋予初始密码
        if(user.getId()==null){
            user.setPassword(new Md5Hash(initialPassword,user.getUsername()).toBase64());
        }
        userService.saveIngoreNull(user);
        return JsonMessage.successed(null,I18nUtil.getMessage("COMMON.SAVE_SUCCESSED"));
    }


    /**
     * 删除用户
     * @param userIdArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户",notes = "删除用户")
    @ApiImplicitParam(name = "userIdArr",value = "用户id数组",paramType = "query")
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除用户")})
    public JsonMessage<String> delete(@RequestParam Long[] userIdArr){
        userService.delete(userIdArr);
        return JsonMessage.successed(null,I18nUtil.getMessage("COMMON.DELETE_SUCCESSED"));
    }

    /**
     * 字段唯一性验证
     * @param fieldName
     * @param val
     * @return
     */
    @RequestMapping(value = "/isUniqueCheck",method = RequestMethod.GET)
    @ApiOperation(value = "字段唯一性验证",notes = "字段唯一性验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fieldName",value = "字段名称",dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "fieldValue",value = "字段的值",dataType = "String",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "true(可用) false(不可用)")})
    public JsonMessage<Object> isUniqueCheck(
            @RequestParam(value = "fieldName",required = true) String fieldName,
            @RequestParam(value = "fieldValue",required = true) String val){
        boolean flag = userService.isUnique(fieldName, val);
        if (flag==false){
            return JsonMessage.failed(I18nUtil.getMessage("IsAvailable_FALSE"));
        }else {
            return JsonMessage.successed(null);
        }
    }

}
