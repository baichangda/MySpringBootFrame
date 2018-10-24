package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.rdb.controller.BaseController;
import com.bcd.base.define.SuccessDefine;
import com.bcd.base.message.JsonMessage;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import com.bcd.sys.bean.PermissionBean;
import com.bcd.sys.service.PermissionService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/permission")
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;



    /**
     * 查询权限列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询权限列表",notes = "查询权限列表")
    public JsonMessage<List<PermissionBean>> list(
            @ApiParam(value = "id",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "角色名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "编码")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "关联角色id",example="1")
            @RequestParam(value = "roleId",required = false) Long roleId
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("roleId",roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(permissionService.findAll(condition));
    }

    /**
     * 查询权限分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询权限列表",notes = "查询权限分页")
    public JsonMessage<Page<PermissionBean>> page(
            @ApiParam(value = "id",example="1")
            @RequestParam(value = "id",required = false) Long id,
            @ApiParam(value = "角色名称")
            @RequestParam(value = "name",required = false) String name,
            @ApiParam(value = "编码")
            @RequestParam(value = "code",required = false) String code,
            @ApiParam(value = "备注")
            @RequestParam(value = "remark",required = false) String remark,
            @ApiParam(value = "关联角色id",example="1")
            @RequestParam(value = "roleId",required = false) Long roleId,
            @ApiParam(value = "分页参数(页数)",example="1")
            @RequestParam(value = "pageNum",required = false)Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",example="20")
            @RequestParam(value = "pageSize",required = false) Integer pageSize
        ){
        Condition condition= Condition.and(
            new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
            new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
            new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
            new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE),
            new NumberCondition("roleId",roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(permissionService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存权限
     * @param permission
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ApiOperation(value = "保存权限",notes = "保存权限")
    public JsonMessage save(@ApiParam(value = "权限实体") @RequestBody PermissionBean permission){
        permissionService.save(permission);
        return SuccessDefine.SUCCESS_SAVE.toJsonMessage();
    }


    /**
     * 删除权限
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除权限",notes = "删除权限")
    public JsonMessage delete(@ApiParam(value = "权限id数组") @RequestParam Long[] ids){
        permissionService.deleteById(ids);
        return SuccessDefine.SUCCESS_DELETE.toJsonMessage();
    }

}
