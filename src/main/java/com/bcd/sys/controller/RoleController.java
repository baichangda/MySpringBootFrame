package com.bcd.sys.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.support_satoken.anno.NotePermission;
import com.bcd.base.support_satoken.anno.SaCheckNotePermissions;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import com.bcd.sys.bean.RoleBean;
import com.bcd.sys.service.RoleService;
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
@RequestMapping("/api/sys/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;


    /**
     * 查询角色列表
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.role_search)
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description = "查询角色列表")
    @ApiResponse(responseCode = "200", description = "角色列表")
    public JsonMessage<List<RoleBean>> list(
            @Parameter(description = "编码") @RequestParam(required = false) String code,
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "关联机构编码") @RequestParam(required = false) String orgCode,
            @Parameter(description = "备注") @RequestParam(required = false) String remark
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("orgCode", orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(roleService.findAll(condition));
    }

    /**
     * 查询角色分页
     *
     * @return
     */
    @SaCheckNotePermissions(NotePermission.role_search)
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description = "查询角色列表")
    @ApiResponse(responseCode = "200", description = "角色分页结果集")
    public JsonMessage<Page<RoleBean>> page(
            @Parameter(description = "编码") @RequestParam(required = false) String code,
            @Parameter(description = "主键") @RequestParam(required = false) Long id,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "关联机构编码") @RequestParam(required = false) String orgCode,
            @Parameter(description = "备注") @RequestParam(required = false) String remark,
            @Parameter(description = "分页参数(页数)") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "分页参数(页大小)") @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        Condition condition = Condition.and(
                new StringCondition("code", code, StringCondition.Handler.ALL_LIKE),
                new NumberCondition("id", id, NumberCondition.Handler.EQUAL),
                new StringCondition("name", name, StringCondition.Handler.ALL_LIKE),
                new StringCondition("orgCode", orgCode, StringCondition.Handler.RIGHT_LIKE),
                new StringCondition("remark", remark, StringCondition.Handler.ALL_LIKE)
        );
        return JsonMessage.success(roleService.findAll(condition, PageRequest.of(pageNum - 1, pageSize)));
    }

    /**
     * 保存角色
     *
     * @param role
     * @return
     */
    @SaCheckNotePermissions(NotePermission.role_edit)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @Operation(description = "保存角色")
    @ApiResponse(responseCode = "200", description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色实体") @Validated @RequestBody RoleBean role) {
        roleService.save(role);
        return JsonMessage.success().message("保存成功");
    }


    /**
     * 删除角色
     *
     * @param ids
     * @return
     */
    @SaCheckNotePermissions(NotePermission.role_edit)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Operation(description = "删除角色")
    @ApiResponse(responseCode = "200", description = "删除结果")
    public JsonMessage delete(@Parameter(description = "角色id数组") @RequestParam Long[] ids) {
        //验证删除权限
        roleService.deleteByIds(ids);
        return JsonMessage.success().message("删除成功");
    }

}
