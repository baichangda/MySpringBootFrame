package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.jackson.impl.SimpleFilterBean;
import com.bcd.base.json.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.bcd.rdb.controller.BaseController;
import com.bcd.rdb.util.FilterUtil;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.service.RoleService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2017/5/10.
 */
@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/role")
public class RoleController extends BaseController{
    @Autowired
    private RoleService roleService;

    /**
     * 查询所有角色
     * @param name
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ApiOperation(value="查询所有角色列表",notes = "查询所有角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "角色id",dataType = "Long",paramType = "query"),
            @ApiImplicitParam(name = "name",value = "角色名称", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "code",value = "角色编码", dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "pageNum",value = "当前页数(分页参数)",dataType = "int",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页显示记录数(分页参数)",dataType = "int",paramType = "query")
    })
    @ApiResponses(value={@ApiResponse(code=200,message = "所有角色列表")})
    public JsonMessage list(@RequestParam(value = "id",required = false) Long id,
            @RequestParam(value = "name",required = false) String name,
                                        @RequestParam(value = "code",required = false) String code,
                                        @RequestParam(value = "pageNum",required = false)Integer pageNum,
                                        @RequestParam(value = "pageSize",required = false) Integer pageSize){
        SimpleFilterBean[] filters= FilterUtil.getOneDeepJsonFilter(RoleBean.class);
        Condition condition= Condition.and(
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("code",code, StringCondition.Handler.ALL_LIKE)
        );
        if(pageNum==null||pageSize==null){
            return JsonMessage.success(JsonUtil.toJSONResult(roleService.findAll(condition),filters));
        }else{
            return JsonMessage.success(JsonUtil.toJSONResult(roleService.findAll(condition,new PageRequest(pageNum-1,pageSize)),filters));
        }
    }

    /**
     * 保存角色
     * @param role
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存角色",notes = "保存角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "role",value = "角色实体",dataType = "SysRoleDTO",paramType = "body"),
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "保存角色")})
    public JsonMessage save(@RequestBody RoleBean role){
        roleService.save(role);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();

    }


    /**
     * 删除角色
     * @param roleIdArr
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除角色",notes = "删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleIdArr",value = "角色id数组",paramType = "query")
    })
    @ApiResponses(value = {@ApiResponse(code = 200,message = "删除角色")})
    public JsonMessage delete(@RequestParam Long[] roleIdArr){
        roleService.delete(roleIdArr);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }



}
