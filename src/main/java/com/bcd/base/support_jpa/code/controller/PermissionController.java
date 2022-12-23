package com.bcd.base.support_jpa.code.controller;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.*;
import com.bcd.base.controller.BaseController;
import com.bcd.base.message.JsonMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import com.bcd.base.support_jpa.code.bean.PermissionBean;
import com.bcd.base.support_jpa.code.service.PermissionService;

@SuppressWarnings(value = "unchecked")
@RestController
@RequestMapping("/api/code/permission")
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 查询权限列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Operation(description="查询权限列表")
    @ApiResponse(responseCode = "200",description = "权限列表")
    public JsonMessage<List<PermissionBean>> list(
        @Parameter(description = "编码") @RequestParam(required = false) String code,
        @Parameter(description = "创建时间开始") @RequestParam(required = false) Date createTimeBegin,
        @Parameter(description = "创建时间结束") @RequestParam(required = false) Date createTimeEnd,
        @Parameter(description = "创建人id") @RequestParam(required = false) Long createUserId,
        @Parameter(description = "创建人姓名") @RequestParam(required = false) String createUserName,
        @Parameter(description = "id") @RequestParam(required = false) Long id,
        @Parameter(description = "角色名称") @RequestParam(required = false) String name,
        @Parameter(description = "备注") @RequestParam(required = false) String remark,
        @Parameter(description = "更新时间开始") @RequestParam(required = false) Date updateTimeBegin,
        @Parameter(description = "更新时间结束") @RequestParam(required = false) Date updateTimeEnd,
        @Parameter(description = "更新人id") @RequestParam(required = false) Long updateUserId,
        @Parameter(description = "更新人姓名") @RequestParam(required = false) String updateUserName
    ){
        Condition condition= Condition.and(
           new StringCondition("code",code),
           new DateCondition("createTime",createTimeBegin, DateCondition.Handler.GE),
           new DateCondition("createTime",createTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("createUserId",createUserId),
           new StringCondition("createUserName",createUserName),
           new NumberCondition("id",id),
           new StringCondition("name",name),
           new StringCondition("remark",remark),
           new DateCondition("updateTime",updateTimeBegin, DateCondition.Handler.GE),
           new DateCondition("updateTime",updateTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("updateUserId",updateUserId),
           new StringCondition("updateUserName",updateUserName)
        );
        return JsonMessage.success(permissionService.findAll(condition));
    }

    /**
     * 查询权限分页
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @Operation(description="查询权限分页")
    @ApiResponse(responseCode = "200",description = "权限分页结果集")
    public JsonMessage<Page<PermissionBean>> page(
        @Parameter(description = "编码") @RequestParam(required = false) String code,
        @Parameter(description = "创建时间开始") @RequestParam(required = false) Date createTimeBegin,
        @Parameter(description = "创建时间结束") @RequestParam(required = false) Date createTimeEnd,
        @Parameter(description = "创建人id") @RequestParam(required = false) Long createUserId,
        @Parameter(description = "创建人姓名") @RequestParam(required = false) String createUserName,
        @Parameter(description = "id") @RequestParam(required = false) Long id,
        @Parameter(description = "角色名称") @RequestParam(required = false) String name,
        @Parameter(description = "备注") @RequestParam(required = false) String remark,
        @Parameter(description = "更新时间开始") @RequestParam(required = false) Date updateTimeBegin,
        @Parameter(description = "更新时间结束") @RequestParam(required = false) Date updateTimeEnd,
        @Parameter(description = "更新人id") @RequestParam(required = false) Long updateUserId,
        @Parameter(description = "更新人姓名") @RequestParam(required = false) String updateUserName,
        @Parameter(description = "分页参数(页数)")  @RequestParam(required = false,defaultValue = "1")Integer pageNum,
        @Parameter(description = "分页参数(页大小)") @RequestParam(required = false,defaultValue = "20") Integer pageSize
    ){
        Condition condition= Condition.and(
           new StringCondition("code",code),
           new DateCondition("createTime",createTimeBegin, DateCondition.Handler.GE),
           new DateCondition("createTime",createTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("createUserId",createUserId),
           new StringCondition("createUserName",createUserName),
           new NumberCondition("id",id),
           new StringCondition("name",name),
           new StringCondition("remark",remark),
           new DateCondition("updateTime",updateTimeBegin, DateCondition.Handler.GE),
           new DateCondition("updateTime",updateTimeEnd, DateCondition.Handler.LE),
           new NumberCondition("updateUserId",updateUserId),
           new StringCondition("updateUserName",updateUserName)
        );
        return JsonMessage.success(permissionService.findAll(condition,PageRequest.of(pageNum-1,pageSize)));
    }

    /**
     * 保存权限
     * @param permission
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @Operation(description = "保存权限")
    @ApiResponse(responseCode = "200",description = "保存结果")
    public JsonMessage save(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "权限实体") @Validated @RequestBody PermissionBean permission){
        permissionService.save(permission);
        return JsonMessage.success();
    }


    /**
     * 删除权限
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.DELETE)
    @Operation(description = "删除权限")
    @ApiResponse(responseCode = "200",description = "删除结果")
    public JsonMessage delete(@Parameter(description = "权限id数组") @RequestParam Long[] ids){
        permissionService.deleteAllById(ids);
        return JsonMessage.success();
    }

}
