package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.shiro.ShiroUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.service.RoleService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/role")
@Api(tags = "角色管理/RoleController")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;



    /**
     * 查询角色列表
     * @return
     */
    @RequiresNotePermissions(NotePermission.role_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value="查询角色列表",notes = "查询角色列表")
    @ApiResponse(code = 200,message = "角色列表")
    public JsonMessage<List<RoleBean>> list(
            @ApiParam(value = "编码") @RequestParam(value = "code", required = false) String code,
            @ApiParam(value = "主键") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "角色名称") @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "关联机构编码") @RequestParam(value = "orgCode", required = false) String orgCode,
            @ApiParam(value = "备注") @RequestParam(value = "remark", required = false) String remark
    ){
        Condition condition= Condition.and(
                new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("orgCode",orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success().withData(roleService.findAll(condition));
    }

    /**
     * 查询角色分页
     * @return
     */
    @RequiresNotePermissions(NotePermission.role_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value="查询角色列表",notes = "查询角色分页")
    @ApiResponse(code = 200,message = "角色分页结果集")
    public JsonMessage<Page<RoleBean>> page(
            @ApiParam(value = "编码") @RequestParam(value = "code", required = false) String code,
            @ApiParam(value = "主键") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "角色名称") @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "关联机构编码") @RequestParam(value = "orgCode", required = false) String orgCode,
            @ApiParam(value = "备注") @RequestParam(value = "remark", required = false) String remark,
            @ApiParam(value = "分页参数(页数)",defaultValue = "1")  @RequestParam(value = "pageNum",required = false,defaultValue = "1")Integer pageNum,
            @ApiParam(value = "分页参数(页大小)",defaultValue = "20") @RequestParam(value = "pageSize",required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
                new StringCondition("code",code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id",id, NumberCondition.Handler.EQUAL),
                new StringCondition("name",name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("orgCode",orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("remark",remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success().withData(roleService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存角色
     * @param role
     * @return
     */
    @RequiresNotePermissions(NotePermission.role_edit)
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
    @RequiresNotePermissions(NotePermission.role_edit)
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @ApiOperation(value = "删除角色",notes = "删除角色")
    @ApiResponse(code = 200,message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "角色id数组") @RequestParam Long[] ids){
        //验证删除权限
        roleService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
