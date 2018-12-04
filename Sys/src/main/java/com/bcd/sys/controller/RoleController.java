package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.service.RoleService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;



    /**
     * 查询角色列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询角色列表",notes = "查询角色列表")
    @ApiResponse(code = 200,message = "角色列表")
    public JsonMessage<List<RoleBean>> list(
            @ApiParam(value = "编码")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "角色名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark
        ){
        Condition condition= Condition.and(
            new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(roleService.findAll(condition));
    }

    /**
     * 查询角色分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询角色列表",notes = "查询角色分页")
    @ApiResponse(code = 200,message = "角色分页结果集")
    public JsonMessage<Page<RoleBean>> page(
            @ApiParam(value = "编码")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "主键",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "角色名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        Condition condition= Condition.and(
            new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(roleService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存角色
     * @param role
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存角色",notes = "保存角色")
    @ApiResponse(code = 200,message = "保存结果")
    public JsonMessage save(@ApiParam(value = "角色实体") @Validated @RequestBody RoleBean role){
        roleService.save(role);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除角色
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除角色",notes = "删除角色")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "角色id数组") @RequestParam Long[] ids){
        roleService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
