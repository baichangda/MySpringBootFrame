package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.shiro.anno.RequiresNotePermissions;
import com.bcd.base.config.shiro.data.NotePermission;
import com.bcd.base.controller.BaseController;
import com.bcd.base.define.MessageDefine;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.PermissionBean;
import com.bcd.sys.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/permission")
@Api(tags = "权限管理/PermissionController")
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;


    /**
     * 查询权限列表
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.permission_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "查询权限列表", notes = "查询权限列表")
    @ApiResponse(code = 200, message = "权限列表")
    public JsonMessage<List<PermissionBean>> list(
            @ApiParam(value = "编码") @RequestParam(value = "code", required = false) String code,
            @ApiParam(value = "id") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "角色名称") @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "备注") @RequestParam(value = "remark", required = false) String remark,
            @ApiParam(value = "关联角色id") @RequestParam(value = "roleId", required = false) Long roleId
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("roleId", roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success().withData(permissionService.findAll(condition));
    }

    /**
     * 查询权限分页
     *
     * @return
     */
    @RequiresNotePermissions(NotePermission.permission_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value = "查询权限列表", notes = "查询权限分页")
    @ApiResponse(code = 200, message = "权限分页结果集")
    public JsonMessage<Page<PermissionBean>> page(
            @ApiParam(value = "编码") @RequestParam(value = "code", required = false) String code,
            @ApiParam(value = "id") @RequestParam(value = "id", required = false) Long id,
            @ApiParam(value = "角色名称") @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "备注") @RequestParam(value = "remark", required = false) String remark,
            @ApiParam(value = "关联角色id") @RequestParam(value = "roleId", required = false) Long roleId,
            @ApiParam(value = "分页参数(页数)", defaultValue = "1") @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @ApiParam(value = "分页参数(页大小)", defaultValue = "20") @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("roleId", roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success().withData((permissionService.findAll(condition, PageRequest.of(pageNum - 1, pageSize))));
    }

    /**
     * 保存权限
     *
     * @param permission
     * @return
     */
    @RequiresNotePermissions(NotePermission.permission_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "保存权限", notes = "保存权限")
    @ApiResponse(code = 200, message = "保存结果")
    public JsonMessage save(@ApiParam(value = "权限实体") @Validated @RequestBody PermissionBean permission) {
        permissionService.save(permission);
        return MessageDefine.SUCCESS_SAVE.toJsonMessage(true);
    }


    /**
     * 删除权限
     *
     * @param ids
     * @return
     */
    @RequiresNotePermissions(NotePermission.permission_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除权限", notes = "删除权限")
    @ApiResponse(code = 200, message = "删除结果")
    public JsonMessage delete(@ApiParam(value = "权限id数组") @RequestParam Long[] ids) {
        permissionService.deleteById(ids);
        return MessageDefine.SUCCESS_DELETE.toJsonMessage(true);
    }

}
