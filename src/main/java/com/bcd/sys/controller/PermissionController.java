package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.support_satoken.anno.NotePermission;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.PermissionBean;
import com.bcd.sys.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/sys/permission")
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;


    /**
     * 查询权限列表
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.permission_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description = "查询权限列表")
    @ApiResponse(responseCode = "200", description = "权限列表")
    public JsonMessage<List<PermissionBean>> list(
            @Parameter(description = "编码") @RequestParam(required = false) String code,
            @Parameter(description = "id") @RequestParam(required = false) Long id,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "备注") @RequestParam(required = false) String remark,
            @Parameter(description = "关联角色id") @RequestParam(required = false) Long roleId
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("roleId", roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(permissionService.list(condition));
    }

    /**
     * 查询权限分页
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.permission_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description = "查询权限列表")
    @ApiResponse(responseCode = "200", description = "权限分页结果集")
    public JsonMessage<Page<PermissionBean>> page(
            @Parameter(description = "编码") @RequestParam(required = false) String code,
            @Parameter(description = "id") @RequestParam(required = false) Long id,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "备注") @RequestParam(required = false) String remark,
            @Parameter(description = "关联角色id") @RequestParam(required = false) Long roleId,
            @Parameter(description = "分页参数(页数)") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "分页参数(页大小)") @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("roleId", roleId, NumberCondition.Handler.EQUAL)
        );
        return JsonMessage.success(permissionService.page(condition, PageRequest.of(pageNum - 1, pageSize)));
    }

    /**
     * 保存权限
     *
     * @param permission
     * @return
     */
    @SaCheckNotePermissions(NotePermission.permission_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @Operation(description = "保存权限")
    @ApiResponse(responseCode = "200", description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "权限实体") @Validated @RequestBody PermissionBean permission) {
        permissionService.save(permission);
        return JsonMessage.success().message("保存成功");
    }


    /**
     * 删除权限
     *
     * @param ids
     * @return
     */
    @SaCheckNotePermissions(NotePermission.permission_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Operation(description = "删除权限")
    @ApiResponse(responseCode = "200", description = "删除结果")
    public JsonMessage delete(@Parameter(description = "权限id数组") @RequestParam long[] ids) {
        permissionService.delete(ids);
        return JsonMessage.success().message("删除成功");
    }

}
